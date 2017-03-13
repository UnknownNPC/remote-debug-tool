package com.github.unknownnpc.debugtesttool.transport

import com.github.unknownnpc.debugtesttool.domain.{TransportCommand, TransportExecutionResult}

import scala.concurrent.Future

trait Transport {

  def executeCommand(command: TransportCommand): Future[TransportExecutionResult]

}

