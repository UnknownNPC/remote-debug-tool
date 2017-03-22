package com.github.unknownnpc.debugtesttool.util

import java.io.File
import java.nio.file.Paths

import com.github.unknownnpc.debugtesttool.domain.Port
import org.slf4j.LoggerFactory

import scala.sys.process._

trait JdkDebugProcessUtil {

  def runJavaClassInDebugMode(javaClassName: String,
                              debugPort: Port,
                              args: String = "",
                              javaPath: String = "java"): Process = {

    val log = LoggerFactory.getLogger(this.getClass)

    val runJavaClassCommand = Seq(
      s"$javaPath"
      , "-Xdebug"
      , s"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=$debugPort"
      , s"$javaClassName"
      , s"$args"
    )

    val processLog = ProcessLogger(message => log.debug(s"External process message: [$message]"))
    Process(
      tuneCommandForMultiplatform(runJavaClassCommand),
      new File(absolutePathToResourceFolderBy("/jdi"))
    ).run(processLog)
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
