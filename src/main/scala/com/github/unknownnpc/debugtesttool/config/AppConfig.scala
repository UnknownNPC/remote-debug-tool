package com.github.unknownnpc.debugtesttool.config

import java.time.{Duration => Jduration}

import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.report.{ReportFormatter, ReportFormatterFactory}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

trait AppConfig {

  def testTargets: List[TestTarget]

  def testCases: List[TestCase]

  def systemConfig: SystemConfig

}

trait DebugTestToolConfig extends AppConfig {

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

  lazy val testTargets: List[TestTarget] = {
    configFile.getConfigList(TEST_TARGETS).asScala.map(row =>
      JvmTestTarget(
        row.getInt(ID),
        row.getString(ADDRESS),
        row.getInt(PORT)
      )
    ).toList
  }

  lazy val testCases: List[TestCase] = {
    configFile.getConfigList(TEST_CASES).asScala.map(row => {
      JvmTestCase(
        row.getInt(SERVER_ID),
        row.getInt(BREAKPOINT_LINE),
        row.getString(BREAKPOINT_CLASS_NAME),
        row.getDuration(BREAKPOINT_EVENT_TRIGGER_TIMEOUT),
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
