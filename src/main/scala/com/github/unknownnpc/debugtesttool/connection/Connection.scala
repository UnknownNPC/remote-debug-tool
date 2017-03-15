package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain.{TestInfo, CommandExecutionResult}

import scala.concurrent.Future

trait Connection {

  def executeCommand(debugInfo: TestInfo): Future[CommandExecutionResult]

}

