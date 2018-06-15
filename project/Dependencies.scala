import sbt._

object Dependencies {
  lazy val playJson = "com.typesafe.play" %% "play-json" % "2.6.9"

  lazy val sprayJson = "io.spray" %%  "spray-json" % "1.3.4"

  lazy val argonaut = "io.argonaut" %% "argonaut" % "6.2.1"

  lazy val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.5"

  lazy val uPickle = "com.lihaoyi" %% "upickle" % "0.6.6"
  lazy val uJson = "com.lihaoyi" %% "ujson" % "0.6.6"

  lazy val json4sVersion = "3.5.4"
  lazy val json4sNative = "org.json4s" %% "json4s-native" % json4sVersion
  lazy val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sVersion

  lazy val jsoniterScalaCore = "com.github.plokhotnyuk.jsoniter-scala" %% "core" % "0.28.0"
  lazy val jsoniterScalaMacros = "com.github.plokhotnyuk.jsoniter-scala" %% "macros" % "0.28.0" % Provided // required only for compile-time

  lazy val circeVersion = "0.9.3"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val circeJackson29 = "io.circe" %% "circe-jackson29" % "0.9.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
