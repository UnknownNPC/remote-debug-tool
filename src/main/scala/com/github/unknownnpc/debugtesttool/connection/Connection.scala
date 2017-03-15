package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain.{TestCase, CommandExecutionResult}

import scala.concurrent.Future

trait Connection {

  def executeCommand(debugInfo: TestCase): Future[CommandExecutionResult]

}

