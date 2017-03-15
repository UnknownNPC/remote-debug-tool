import sbt._
import Keys._

//General block
lazy val commonSettings = Seq(
  name := "remote-debug-test-tool",
  version := "0.1",
  organization := "com.github.unknownnpc",
  version := "2.4-SNAPSHOT",
  scalaVersion := "2.12.1"
)

lazy val projectConfig = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings: _*)
  .settings(packageSettings: _*)


//Dependencies
val akkaVersion = "2.4.17"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1"
  , "com.typesafe.akka" %% "akka-actor" % akkaVersion
  , "ch.qos.logback" % "logback-classic" % "1.1.7"
  , "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


//Packaging block
lazy val packageSettings = Seq(
  resourceDirectory in Compile := (resourceDirectory in Compile).value,
  mappings in Universal += {
  ((resourceDirectory in Compile).value / "application.conf") -> "conf/application.conf"
})