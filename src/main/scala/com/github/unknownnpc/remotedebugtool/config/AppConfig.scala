package com.github.unknownnpc.remotedebugtool.config

import java.time.{Duration => Jduration}

import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.report.{ReportFormatter, ReportFormatterFactory}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

trait AppConfig {

  def servers: List[Target]

  def breakpoints: List[Breakpoint]

  def systemConfig: SystemConfig

}

trait RemoteDebugToolConfig extends AppConfig {

  private lazy val configFile = ConfigFactory.load()

  implicit def asFiniteDuration(d: Jduration): FiniteDuration = Duration.fromNanos(d.toNanos)

  lazy val systemConfig: SystemConfig = {
    val systemConfig = configFile.getConfig(SYSTEM_CONFIG)
    val reportFormatterTypeString = systemConfig.getString(REPORT_FORMATTER)
    SystemConfig(
      Timeout.durationToTimeout(systemConfig.getDuration(REMOTE_VM_REQUEST_TIMEOUT)),
      Timeout.durationToTimeout(systemConfig.getDuration(REMOTE_VM_CONNECTION_IDLE_TIMEOUT)),
      reportFormatterFrom(reportFormatterTypeString)
    )
  }

  lazy val servers: List[Target] = {
    configFile.getConfigList(SERVERS).asScala.map(row =>
      JvmTarget(
        row.getInt(ID),
        row.getString(ADDRESS),
        row.getInt(PORT)
      )
    ).toList
  }

  lazy val breakpoints: List[Breakpoint] = {
    configFile.getConfigList(BREAKPOINTS).asScala.map(row => {
      JvmBreakpoint(
        row.getInt(SERVER_ID),
        row.getInt(LINE),
        row.getString(CLASS_NAME),
        row.getDuration(EVENT_TRIGGER_TIMEOUT),
        row.getString(FIELD_NAME)
      )
    }
    ).toList
  }

  private def reportFormatterFrom(reportFormatterTypeString: String): ReportFormatter = {
    val reportType = ReportFormatterFactory.ReportType.withName(reportFormatterTypeString)
    ReportFormatterFactory.findReportBy(reportType)
  }
}
