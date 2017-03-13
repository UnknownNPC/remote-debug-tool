name := "remote-debug-integration-test-tool"

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.4.17"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1"
  , "com.typesafe.akka" %% "akka-actor" % akkaVersion
  , "ch.qos.logback" % "logback-classic" % "1.1.7"
)