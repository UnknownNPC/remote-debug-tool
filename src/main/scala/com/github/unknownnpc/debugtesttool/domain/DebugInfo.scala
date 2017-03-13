package com.github.unknownnpc.debugtesttool.domain

import com.github.unknownnpc.debugtesttool.action.TestAction

sealed trait DebugInfo {

  def testServerId: TargetId

  def breakPointLine: BreakpointLine

  def testFieldName: TestFieldName

  def testAction: TestAction

}

case class JvmDebugInfo(testServerId: TargetId,
                        breakPointLine: BreakpointLine,
                        testFieldName: TestFieldName,
                        testAction: TestAction) extends DebugInfo
