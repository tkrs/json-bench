import sbt._

object Dependencies {
  lazy val playJson = "com.typesafe.play" %% "play-json" % "2.6.7"
  lazy val sprayJson = "io.spray" %%  "spray-json" % "1.3.3"
  lazy val json4sNative = "org.json4s" %% "json4s-native" % "3.6.0-M3"

  val circeVersion = "0.9.3"
  val circeCore = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
