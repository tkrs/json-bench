import Dependencies._

name := "json-bench"
organization := "com.github.tkrs"
version      := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  playJson,
  sprayJson,
  json4sNative,
  circeCore,
  circeGeneric,
  circeParser,
  argonaut,
  scalaTest % Test
)

enablePlugins(JmhPlugin)
