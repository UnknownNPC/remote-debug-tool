package com.github.unknownnpc.debugtesttool.config

import com.github.unknownnpc.debugtesttool.action.{NotNull, TestAction, UnknownAction}
import com.github.unknownnpc.debugtesttool.domain._
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

trait AppConfig {

  def testTargets: List[TestTarget]

  def testInfos: List[TestInfo]

}

trait DebugTestToolConfig extends AppConfig {

  private val configFile: Config = ConfigFactory.load()

  override def testTargets: List[TestTarget] = {
    configFile.getConfigList(DEBUG_TARGETS).asScala.map(row =>
      JvmTestTarget(
        row.getInt(ID),
        row.getString(ADDRESS),
        row.getInt(PORT)
      )
    ).toList
  }

  override def testInfos: List[TestInfo] = {
    configFile.getConfigList(DEBUG_INFOS).asScala.map(row => {
      val testAction: TestAction = testActionFrom(row.getString(TEST_ACTION))
      JvmTestInfo(
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
