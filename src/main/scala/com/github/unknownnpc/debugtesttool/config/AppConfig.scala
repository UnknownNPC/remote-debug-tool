package com.github.unknownnpc.debugtesttool.config

import java.time.{Duration => Jduration}

import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.report.ReportFactory.ReportType
import com.github.unknownnpc.debugtesttool.report.{ReportExecutor, ReportFactory}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

trait AppConfig {

  def testTargets: List[TestTarget]

  def testCases: List[TestCase]

  def systemConfig: SystemConfig

}

trait DebugTestToolConfig extends AppConfig {

  private val configFile: Config = ConfigFactory.load()

  implicit def asFiniteDuration(d: Jduration): FiniteDuration = Duration.fromNanos(d.toNanos)

  override def systemConfig: SystemConfig = {
    val systemConfig = configFile.getConfig(SYSTEM_CONFIG)
    val reportType = systemConfig.getString(REPORT_TYPE)
    SystemConfig(
      Timeout.durationToTimeout(systemConfig.getDuration(REMOTE_VM_REQUEST_TIMEOUT)),
      Timeout.durationToTimeout(systemConfig.getDuration(REMOTE_VM_CONNECTION_IDLE_TIMEOUT)),
      reportExecutorFrom(reportType)
    )
  }

  override def testTargets: List[TestTarget] = {
    configFile.getConfigList(TEST_TARGETS).asScala.map(row =>
      JvmTestTarget(
        row.getInt(ID),
        row.getString(ADDRESS),
        row.getInt(PORT)
      )
    ).toList
  }

  override def testCases: List[TestCase] = {
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

  private def reportExecutorFrom(configValue: String): ReportExecutor = {
    val reportType = ReportType.withName(configValue)
    ReportFactory.findReportBy(reportType)
  }
}

object DebugTestToolConfig extends DebugTestToolConfig
