package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging}
import com.github.unknownnpc.debugtesttool.connection.VmJdiConnection
import com.github.unknownnpc.debugtesttool.domain.{TestCase, TestTarget}

import scala.concurrent.Future

class JdiVmConnectionActor(testTarget: TestTarget) extends Actor with ActorLogging {

  val jdiVmConnection = VmJdiConnection(testTarget.address, testTarget.port)

  override def receive = {

    case e: TestCase => sender ! jdiVmConnection.executeCommand(e)

    case _ => sender ! Future.failed(new Exception("Unable to handle incoming message"))

  }

}
