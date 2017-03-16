package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain.{BreakpointClassName, BreakpointLine, CommandExecutionResult, TestCase}

import scala.concurrent.Future

trait Connection {

  def lockVm()

  def unlockVm()

  def setBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def removeBreakpoint()

  def findValue(debugInfo: TestCase): Future[CommandExecutionResult]

}
