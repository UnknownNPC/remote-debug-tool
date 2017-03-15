package com.github.unknownnpc.debugtesttool.config

import java.time.{Duration => Jduration}

import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.action.{NotNull, TestAction, UnknownAction}
import com.github.unknownnpc.debugtesttool.domain._
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
    SystemConfig(
      Timeout.durationToTimeout(systemConfig.getDuration(REMOTE_VM_REQUEST_TIMEOUT))
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
      val testAction: TestAction = testActionFrom(row.getString(TEST_ACTION))
      JvmTestCase(
        row.getInt(SERVER_ID),
        row.getInt(BREAKPOINT_LINE),
        row.getString(BREAKPOINT_THREAD_NAME),
        row.getString(BREAKPOINT_CLASS_NAME),
        row.getString(FIELD_NAME),
        testAction
      )
    }
    ).toList
  }

  private def testActionFrom(configValue: String): TestAction = {
    configValue match {
      case NOT_NULL_ACTION => NotNull
      case _ => UnknownAction
    }
  }
}

object DebugTestToolConfig extends DebugTestToolConfig
