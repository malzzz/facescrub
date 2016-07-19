name := "facescrub"

organization := "co.quine"

version := "1.0"

scalaVersion := "2.11.8"

lazy val versions = new {
  val fs = "0.9.0-M6"
}

resolvers ++= Seq[Resolver](
  "Quine Releases"                    at "s3://releases.repo.quine.co",
  "Quine Snapshots"                   at "s3://snapshots.repo.quine.co",
  "Local Maven"                       at Path.userHome.asFile.toURI.toURL + ".m2/repository",
  "Typesafe repository snapshots"     at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"      at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                     at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                 at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"                at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                  at "http://oss.sonatype.org/content/repositories/staging",
  "Sonatype release Repository"       at "http://oss.sonatype.org/service/local/staging/deploy/maven2/",
  "Java.net Maven2 Repository"        at "http://download.java.net/maven/2/",
  "Twitter Repository"                at "http://maven.twttr.com",
  "Websudos releases"                 at "https://dl.bintray.com/websudos/oss-releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe"                % "config"                          % "1.3.0",
  "org.scalaj"                  %% "scalaj-http"                    % "2.3.0",
  "co.fs2"                      %% "fs2-core"                       % versions.fs,
  "co.fs2"                      %% "fs2-io"                         % versions.fs,
  "co.fs2"                      %% "fs2-cats"                       % "0.1.0-M6",
  "org.typelevel"               %% "cats"                           % "0.6.0"
)
    