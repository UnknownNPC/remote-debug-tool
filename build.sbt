import sbt.{Resolver, _}
import Keys._

//General block
lazy val commonSettings = Seq(
  name := "remote-debug-tool",
  version := "0.1",
  organization := "com.github.unknownnpc",
  scalaVersion := "2.12.3"
)

lazy val projectConfig = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings: _*)
  .settings(packageSettings)
  .settings(libraryDependencies ++= projectDependencies)
  .settings(resolvers ++= projectResolvers)


lazy val projectDependencies = Seq(
    "com.typesafe" % "config" % "1.3.1"
  , "com.typesafe.akka" %% "akka-actor" % "2.6.1"
  , "com.typesafe.akka" %% "akka-slf4j" % "2.6.1"
  , "ch.qos.logback" % "logback-classic" % "1.1.7"
  , "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  , "com.typesafe.akka" %% "akka-testkit" % "2.6.1" % "test"
  , "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test"
)

lazy val projectResolvers = Seq(
  Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)
)

//Packaging block
def packageSettings: Seq[Setting[_]] = {
  Seq(
    mappings in Universal ++= Seq(
      resourceToConfigDir((resourceDirectory in Compile).value, "application.conf"),
      resourceToConfigDir((resourceDirectory in Compile).value, "logback.xml"))
  ) ++ Seq(
    mappings in Universal ~= { r =>
      r.filterNot(f => resourcesToExcludeFromJar.contains(f._2))
    }
  ) ++ Seq(
    scriptClasspath := Seq("../conf") ++ NativePackagerKeys.scriptClasspath.value,
    executableScriptName := "run"
  )
}

def resourceToConfigDir(base: File, fileName: String) = (base / fileName) -> s"conf/$fileName"

def resourcesToExcludeFromJar = Seq(
  "application.conf",
  "logback.xml"
)
