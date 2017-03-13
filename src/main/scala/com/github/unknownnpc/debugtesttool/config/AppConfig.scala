package com.github.unknownnpc.debugtesttool.config

import com.github.unknownnpc.debugtesttool.action.{NotNull, TestAction, UnknownAction}
import com.github.unknownnpc.debugtesttool.domain._
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

trait AppConfig {

  def testTargets: List[ExecutionPair]

}

trait DebugTestToolConfig extends AppConfig {

  private val configFile: Config = ConfigFactory.load()

  override def testTargets: List[ExecutionPair] = {
    executionPairFrom(readDebugTargets, readDebugDetails)
  }

  private def readDebugTargets: List[DebugTarget] = {
    configFile.getConfigList(DEBUG_TARGETS).asScala.map(row =>
      JvmDebugTarget(
        row.getInt(ID),
        row.getString(ADDRESS),
        row.getInt(PORT)
      )
    ).toList
  }

  private def readDebugDetails: List[DebugInfo] = {
    configFile.getConfigList(DEBUG_DETAILS).asScala.map(row => {
      val testAction: TestAction = testActionFrom(row.getString(TEST_ACTION))
      JvmDebugInfo(
        row.getInt(TEST_SERVER_ID),
        row.getInt(BREAKPOINT_LINE),
        row.getString(TEST_FIELD_NAME),
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

  private def executionPairFrom(targets: List[DebugTarget],
                                details: List[DebugInfo]): List[ExecutionPair] = {
    for {
      target <- targets
      targetIdWithDetails <- details.groupBy(_.testServerId)
      if target.id == targetIdWithDetails._1
    } yield ExecutionPair(target, targetIdWithDetails._2)
  }
}

object DebugTestToolConfig extends DebugTestToolConfig
