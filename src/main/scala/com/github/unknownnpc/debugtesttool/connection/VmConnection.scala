package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._

trait VmConnection {

  def lockVm()

  def unlockVm()

  def connect()

  def disconnect()

  def enableBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def disableBreakpoint()

  def findValue(fieldName: FieldName, searchTimeout: BreakpointEventTriggerTimeout): Option[TestCaseValue]

}
