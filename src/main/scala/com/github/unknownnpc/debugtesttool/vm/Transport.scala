package com.github.unknownnpc.debugtesttool.vm

import com.github.unknownnpc.debugtesttool.domain.{TestInfo, TransportExecutionResult}

import scala.concurrent.Future

trait Transport {

  def executeCommand(debugInfo: TestInfo): Future[TransportExecutionResult]

}

