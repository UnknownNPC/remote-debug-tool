package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._

import scala.util.Try

trait VmConnection {

  def lockVm()

  def unlockVm()

  def setBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def removeBreakpoint()

  def findValue(breakPointThreadName: BreakpointThreadName,
                fieldName: FieldName,
                searchTimeout: BreakpointWaiting): Option[CommandExecutionResult]

}
