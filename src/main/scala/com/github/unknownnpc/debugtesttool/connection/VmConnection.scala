package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._

trait VmConnection {

  def lockVm()

  def unlockVm()

  def setBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def removeBreakpoint()

  def findValue(fieldName: FieldName, searchTimeout: BreakpointWaiting): Option[CommandExecutionResult]

}
