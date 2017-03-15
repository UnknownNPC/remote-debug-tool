package com.github.unknownnpc.debugtesttool.domain

import com.github.unknownnpc.debugtesttool.action.TestAction

sealed trait TestInfo {

  def targetId: ID

  def breakPointLine: BreakpointLine

  def breakPointThreadName: BreakpointThreadName

  def breakPointClassName: BreakpointClassName

  def fieldName: FieldName

  def testAction: TestAction

}

case class JvmTestInfo(targetId: ID,
                       breakPointLine: BreakpointLine,
                       breakPointThreadName: BreakpointThreadName = "main",
                       breakPointClassName: BreakpointClassName,
                       fieldName: FieldName,
                       testAction: TestAction) extends TestInfo
