package facescrub

import java.nio.file.{Files, Path}

trait ImageDetector[A] {
  def isImage(a: A): Boolean
  def isJpeg(a: A): Boolean
  def isPng(a: A): Boolean
  def isBmp(a: A): Boolean
  def isDng(a: A): Boolean
  def isFormat[B <: ImageFormat](a: A, b: B): Boolean
  def imageFormat(a: A): Option[ImageFormat]
}

object ImageDetector {
  val validImages = Seq[ImageFormat](JPEG, PNG, BMP, DNG)

  implicit val byteArrayImage = new ImageDetector[Array[Byte]] {
    def isImage(a: Array[Byte]) = validImages.exists(f => a.take(f.sigLen).sameElements(f.sig))
    def isJpeg(a: Array[Byte]) = a.take(JPEG.sigLen).sameElements(JPEG.sig)
    def isPng(a: Array[Byte]) = a.take(PNG.sigLen).sameElements(PNG.sig)
    def isBmp(a: Array[Byte]) = a.take(BMP.sigLen).sameElements(BMP.sig)
    def isDng(a: Array[Byte]) = a.take(DNG.sigLen).sameElements(DNG.sig)
    def isFormat[B <: ImageFormat](a: Array[Byte], b: B) = a.take(b.sigLen).sameElements(b.sig)
    def imageFormat(a: Array[Byte]) = validImages.find(f => a.take(f.sigLen).sameElements(f.sig))
  }

  implicit val javaPathImage = new ImageDetector[Path] {
    def isImage(a: Path) = byteArrayImage.isImage(Files.readAllBytes(a))
    def isJpeg(a: Path) = byteArrayImage.isJpeg(Files.readAllBytes(a))
    def isPng(a: Path) = byteArrayImage.isPng(Files.readAllBytes(a))
    def isBmp(a: Path) = byteArrayImage.isBmp(Files.readAllBytes(a))
    def isDng(a: Path) = byteArrayImage.isDng(Files.readAllBytes(a))
    def isFormat[B <: ImageFormat](a: Path, b: B) = byteArrayImage.isFormat(Files.readAllBytes(a), b)
    def imageFormat(a: Path) = byteArrayImage.imageFormat(Files.readAllBytes(a))
  }
}

sealed trait ImageFormat {
  val sig: Array[Byte]
  def sigLen = sig.length
}

case object JPEG extends ImageFormat {
  val sig = Array[Char](0xFF, 0xD8).map(_.toByte)
}

case object PNG extends ImageFormat {
  val sig = Array[Char](0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).map(_.toByte)
}

case object BMP extends ImageFormat {
  val sig = Array[Char](0x42, 0x4D).map(_.toByte)
}

case object DNG extends ImageFormat {
  val sig = Array[Char](0x44, 0x4E, 0x47).map(_.toByte)
}

