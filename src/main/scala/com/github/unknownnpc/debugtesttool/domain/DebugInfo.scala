package com.github.unknownnpc.debugtesttool.domain

import com.github.unknownnpc.debugtesttool.action.TestAction

sealed trait DebugInfo {

  def testServerId: Id
  def breakPointLine: BreakpointLine
  def testFieldName: TestFieldName
  def testAction: TestAction

}

case class JvmDebugInfo(testServerId: Id,
                        breakPointLine: BreakpointLine,
                        testFieldName: TestFieldName,
                        testAction: TestAction) extends DebugInfo
