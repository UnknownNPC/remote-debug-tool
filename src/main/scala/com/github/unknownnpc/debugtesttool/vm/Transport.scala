package com.github.unknownnpc.debugtesttool.vm

import com.github.unknownnpc.debugtesttool.domain.{DebugInfo, TransportExecutionResult}

import scala.concurrent.Future

trait Transport {

  def executeCommand(debugInfo: DebugInfo): Future[TransportExecutionResult]

}

