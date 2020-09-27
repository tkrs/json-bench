import Dependencies._

ThisBuild / organization := "com.github.tkrs"
ThisBuild / name := "json-bench"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / scalafmtOnCompile := true

libraryDependencies ++= Seq(
  argonaut,
  circeCore,
  circeGeneric,
  circeJackson,
  circeParser,
  jsoniterScalaCore,
  jsoniterScalaMacros,
  sprayJson,
  uJson,
  uPickle,
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
