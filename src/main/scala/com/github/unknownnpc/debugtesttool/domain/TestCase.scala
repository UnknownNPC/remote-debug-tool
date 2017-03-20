package com.github.unknownnpc.debugtesttool.domain

sealed trait TestCase {

  def targetId: ID

  def breakPointLine: BreakpointLine

  def breakPointClassName: BreakpointClassName

  def breakpointEventTriggerTimeout: BreakpointEventTriggerTimeout

  def fieldName: FieldName

}

case class JvmTestCase(targetId: ID,
                       breakPointLine: BreakpointLine,
                       breakPointClassName: BreakpointClassName,
                       breakpointEventTriggerTimeout: BreakpointEventTriggerTimeout,
                       fieldName: FieldName) extends TestCase
