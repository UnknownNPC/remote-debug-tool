import sbt._
import Keys._

//General block
lazy val commonSettings = Seq(
  name := "remote-debug-test-tool",
  version := "0.1",
  organization := "com.github.unknownnpc",
  scalaVersion := "2.12.1"
)

lazy val projectConfig = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings: _*)
  .settings(packageSettings)
  .settings(libraryDependencies ++= projectDependencies)


//Core dependencies
lazy val projectDependencies = Seq(
  "com.typesafe" % "config" % "1.3.1"
  , "com.typesafe.akka" %% "akka-actor" % "2.4.17"
  , "ch.qos.logback" % "logback-classic" % "1.1.7"
  , "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


//Packaging block
lazy val packageSettings = Seq(
  resourceDirectory in Compile := (resourceDirectory in Compile).value
  , mappings in Universal += {
    ((resourceDirectory in Compile).value / "application.conf") -> "conf/application.conf"
  }
)
