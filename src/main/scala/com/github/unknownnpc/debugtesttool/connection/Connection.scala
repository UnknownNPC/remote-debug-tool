package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._

import scala.util.Try

trait Connection {

  def lockVm()

  def unlockVm()

  def setBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def removeBreakpoint()

  def findValue(breakPointThreadName: BreakpointThreadName, fieldName: FieldName): Try[CommandExecutionResult]

}
