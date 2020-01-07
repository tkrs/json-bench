import Dependencies._

ThisBuild / organization := "com.github.tkrs"
ThisBuild / name := "json-bench"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.10"
ThisBuild / scalafmtOnCompile := true

libraryDependencies ++= Seq(
  playJson,
  sprayJson,
  json4sNative,
  json4sJackson,
  jsoniterScalaCore,
  jsoniterScalaMacros,
  circeCore,
  circeGeneric,
  circeParser,
  circeJackson29,
  argonaut,
  jacksonScala,
  uPickle,
  uJson,
  scalaTest % Test
).map(_.withSources)

enablePlugins(JmhPlugin)
scalafmtOnCompile := true // all projects

scalacOptions := Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-Xcheckinit",
  "-Xlint",
  "-Ydelambdafy:method",
  "-opt-inline-from:**",
  "-opt:l:inline",
  "-opt-warnings"
)
