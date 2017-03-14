package com.github.unknownnpc.debugtesttool.domain

import com.github.unknownnpc.debugtesttool.action.TestAction

sealed trait DebugInfo {

  def testServerId: TargetId

  def breakPointLine: BreakpointLine

  def breakPointThreadName: BreakpointThreadName

  def breakPointClassName: BreakpointClassName

  def testFieldName: TestFieldName

  def testAction: TestAction

}

case class JvmDebugInfo(testServerId: TargetId,
                        breakPointLine: BreakpointLine,
                        breakPointThreadName: BreakpointThreadName = "main",
                        breakPointClassName: BreakpointClassName,
                        testFieldName: TestFieldName,
                        testAction: TestAction) extends DebugInfo
