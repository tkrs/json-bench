import sbt._

object Dependencies {
  lazy val sprayJson = "io.spray" %% "spray-json" % "1.3.5"

  lazy val argonaut = "io.argonaut" %% "argonaut" % "6.2.3"

  lazy val uPickle = "com.lihaoyi" %% "upickle" % "0.9.5"
  lazy val uJson   = "com.lihaoyi" %% "ujson"   % "0.9.5"

  lazy val jsoniterVersion     = "2.1.2"
  lazy val jsoniterScalaCore   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion
  lazy val jsoniterScalaMacros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % Provided // required only for compile-time

  lazy val circeVersion = "0.12.3"
  lazy val circeCore    = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser  = "io.circe" %% "circe-parser" % circeVersion

  lazy val circeJackson = "io.circe" %% "circe-jackson210" % "0.12.1"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.0"
}
