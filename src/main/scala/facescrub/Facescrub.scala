package facescrub

import java.io.{ByteArrayInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file._

import scalaj.http._

import cats.data.Xor
import fs2._

object Facescrub {

  case class FacescrubRecord(name: String, gender: String, image_id: Int, face_id: Int, url: String, bbox: BoundingBox)
  case class FacescrubImage(name: String, gender: String, image_id: Int, face_id: Int, image: Array[Byte], bbox: BoundingBox)
  case class BoundingBox(p1: Int, p2: Int, p3: Int, p4: Int)

  lazy val actorsFile = getClass.getResource("/facescrub_actors.txt")
  lazy val actressFile = getClass.getResource("/facescrub_actresses.txt")

  implicit val strategy = Strategy.fromFixedDaemonPool(32)

  val count = new java.util.concurrent.atomic.AtomicInteger(0)

  def parseRecord(line: String)(gender: String): Option[FacescrubRecord] = {
    try {
      val fields = line.split("\\s+")
      val name = fields.takeWhile(s => s.forall(!_.isDigit))
      val image_id = fields(name.length)
      val face_id = fields(name.length + 1)
      val url = fields(name.length + 2)
      val bbox = fields(name.length + 3).split(",")
      val boundingBox = BoundingBox(bbox(0).toInt, bbox(1).toInt, bbox(2).toInt, bbox(3).toInt)
      Some(FacescrubRecord(name.mkString("_").toLowerCase, gender, image_id.toInt, face_id.toInt, url, boundingBox))
    } catch {
      case x: java.lang.ArrayIndexOutOfBoundsException => None
      case _: Throwable => None
    }
  }

  def actorStream: Stream[Task, FacescrubRecord] =
    io.file.readAll[Task](Paths.get(actorsFile.toURI), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .drop(1)
      .map(line => parseRecord(line)("m"))
      .collect( { case Some(x) => x } )

  def actressStream: Stream[Task, FacescrubRecord] =
    io.file.readAll[Task](Paths.get(actressFile.toURI), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .drop(1)
      .map(line => parseRecord(line)("f"))
      .collect( { case Some(x) => x } )

  def imageStreams(savePath: String): Vector[Stream[Task, Unit]] =
    (actorStream merge actressStream).runLog.unsafeRun map { rec =>
      Stream(rec)
        .through(downloadPipe)
        .through(progress)
        .collect { case Some(fsr) => fsr }
        .to(writeSink(savePath))
    }

  def downloadPipe(implicit det: ImageDetector[Array[Byte]]): Pipe[Task, FacescrubRecord, Option[FacescrubImage]] =
    _.evalMap[Task, Task, Option[FacescrubImage]] { rec =>
      Task.delay[Option[FacescrubImage]] {
        Xor.catchNonFatal {
          Http(rec.url).timeout(15000, 25000).option(HttpOptions.followRedirects(true)).asBytes match {
            case resp if resp.is2xx && det.isImage(resp.body) =>
              Some(FacescrubImage(rec.name, rec.gender, rec.image_id, rec.face_id, resp.body, rec.bbox))
            case _ => None
          }
        } getOrElse None
      }
    }

  def writeSink(savePath: String): Sink[Task, FacescrubImage] =
    _.evalMap[Task, Task, Unit] { img =>
      Task.delay {
        val bbox = s"(${img.bbox.p1}x${img.bbox.p2}x${img.bbox.p3}x${img.bbox.p4})"
        val filename = s"${img.gender}-${img.image_id}-${img.face_id}-${img.name}-$bbox"
        val os = new FileOutputStream(s"$savePath/$filename.jpg").getChannel
        val buffer = ByteBuffer.allocate(4096)

        val ic = Channels.newChannel(new ByteArrayInputStream(img.image))

        try {
          while (ic.read(buffer) >= 0 || buffer.position() > 0) {
            buffer.flip()
            os.write(buffer)
            buffer.compact()
          }
        } finally {
          ic.close()
          os.close()
        }
      }
    }

  def progress[A]: Pipe[Task, A, A] =
    _.evalMap[Task, Task, A] { a => Task.delay[A] { println(s"\rProgress: ${count.incrementAndGet} downloaded"); a } }

  val usage =
    """
      |Usage: facescrub /image/save/path
    """.stripMargin

  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println(usage)
    } else {
      val savePath = args(0)
      println(s"Running (saving to: $savePath)")
      concurrent.join[Task, Unit](32)(Stream.pure(imageStreams(savePath):_*)).run.unsafeRun
    }

  }

}