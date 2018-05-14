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
  jacksonScala,
  uPickle,
  uJson,
  scalaTest % Test
).map(_.withSources)

enablePlugins(JmhPlugin)

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-explaintypes",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xcheckinit",
  "-Xfuture",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ydelambdafy:method",
  "-opt-inline-from:**",
  "-opt:l:inline",
  "-opt-warnings"
)
