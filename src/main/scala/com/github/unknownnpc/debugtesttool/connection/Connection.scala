package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain.{BreakpointLine, CommandExecutionResult, TestCase}

import scala.concurrent.Future

trait Connection {

  def lockVm()

  def unlockVm()

  def setBreakpoint(line: BreakpointLine)

  def removeBreakpoint()

  def findValue(debugInfo: TestCase): Future[CommandExecutionResult]

}
