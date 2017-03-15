package com.github.unknownnpc.debugtesttool.util

import java.io.File
import java.nio.file.Paths

import com.github.unknownnpc.debugtesttool.domain.Port

import scala.sys.process._

trait JdkDebugProcessUtil {

  def runJavaClassInDebugMode(jdiTestFolderName: String,
                              javaClassName: String,
                              debugPort: Port,
                              args: String = "",
                              javaPath: String = "java"): Process = {

    val runJavaClassCommand = Seq(
      s"$javaPath"
      , "-Xdebug"
      , s"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$debugPort"
      , s"$javaClassName"
      , s"$args"
    )

    Process(
      tuneCommandForMultiplatform(runJavaClassCommand),
      new File(absolutePathToResourceFolderBy("/jdi") + File.separator + s"$jdiTestFolderName")
    ).run
  }

  private def absolutePathToResourceFolderBy(name: String) = {
    val resource = getClass.getResource(name)
    Paths.get(resource.toURI).toFile.getAbsolutePath
  }

  private def tuneCommandForMultiplatform(command: Seq[String]) = {
    val os = sys.props("os.name").toLowerCase
    os match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _ => command
    }
  }

}
