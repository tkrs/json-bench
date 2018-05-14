import sbt._

object Dependencies {
  lazy val playJson = "com.typesafe.play" %% "play-json" % "2.6.7"

  lazy val sprayJson = "io.spray" %%  "spray-json" % "1.3.3"

  lazy val argonaut = "io.argonaut" %% "argonaut" % "6.2.1"

  lazy val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5"

  lazy val upickle = "com.lihaoyi" %% "upickle" % "0.6.5"
  lazy val uJson = "com.lihaoyi" %% "ujson" % "0.6.5"

  lazy val json4sNative = "org.json4s" %% "json4s-native" % "3.5.3"
  lazy val json4sScalaz = "org.json4s" %% "json4s-scalaz" % "3.5.3"

  lazy val circeVersion = "0.9.3"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
