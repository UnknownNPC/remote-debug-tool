package com.github.unknownnpc.debugtesttool.transport
import com.github.unknownnpc.debugtesttool.domain._
import com.sun.jdi._

import scala.concurrent.Future

case class JdiTransportWrapper(address: TargetAddress, port: TargetPort) extends Transport {

  val vmm = Bootstrap.virtualMachineManager()

  def createConnection(): Unit = {
    val defaultConnector = vmm.defaultConnector()
    val arguments = defaultConnector.defaultArguments()
    arguments.get("port", port)
    arguments.get("hostname", address)
    defaultConnector.launch(arguments)
  }

  override def dropConnection(): Unit = {

  }

  override def executeCommand(command: DebugInfo): Future[TransportExecutionResult] = {

  }
}

