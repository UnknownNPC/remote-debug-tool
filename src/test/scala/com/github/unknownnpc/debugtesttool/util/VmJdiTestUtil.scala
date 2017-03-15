package com.github.unknownnpc.debugtesttool.util

import java.nio.file.Paths

import scala.sys.process._

trait VmJdiTestUtil {

  def runJdiJavaClass(folderPath: String, ClassName: String, debugPort: Int, args: String = "", javaPath: String = "java"): Process = {

    val runJavaClassCommand: Seq[String] = Seq(
      s"$javaPath"
      , "-Xdebug"
      , s"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$debugPort"
      , s"$ClassName"
      , s"$args"
    )

    val cdToFolderCommand: Seq[String] = Seq(
      "cd"
      , jdiAbsolutePath + folderPath, "&&"
    )

    patchOsCommand(cdToFolderCommand ++ runJavaClassCommand).run
  }

  def jdiAbsolutePath = {
    val resource = getClass.getResource("/jdi")
    Paths.get(resource.toURI).toFile.getAbsolutePath
  }

  def patchOsCommand(command: Seq[String]) = {
    val os = sys.props("os.name").toLowerCase
    os match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _ => command
    }
  }

}
