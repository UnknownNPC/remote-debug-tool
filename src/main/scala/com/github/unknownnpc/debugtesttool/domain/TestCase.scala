package com.github.unknownnpc.debugtesttool.domain

import com.github.unknownnpc.debugtesttool.action.TestAction

sealed trait TestCase {

  def targetId: ID

  def breakPointLine: BreakpointLine

  def breakPointThreadName: BreakpointThreadName

  def breakPointClassName: BreakpointClassName

  def breakpointWaiting: BreakpointWaiting

  def fieldName: FieldName

  def testAction: TestAction

}

case class JvmTestCase(targetId: ID,
                       breakPointLine: BreakpointLine,
                       breakPointThreadName: BreakpointThreadName = "main",
                       breakPointClassName: BreakpointClassName,
                       breakpointWaiting: BreakpointWaiting,
                       fieldName: FieldName,
                       testAction: TestAction) extends TestCase
