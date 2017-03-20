package com.github.unknownnpc.debugtesttool.domain

sealed trait TestCase {

  def targetId: ID

  def breakPointLine: BreakpointLine

  def breakPointClassName: BreakpointClassName

  def breakpointWaiting: BreakpointWaiting

  def fieldName: FieldName

}

case class JvmTestCase(targetId: ID,
                       breakPointLine: BreakpointLine,
                       breakPointClassName: BreakpointClassName,
                       breakpointWaiting: BreakpointWaiting,
                       fieldName: FieldName) extends TestCase
