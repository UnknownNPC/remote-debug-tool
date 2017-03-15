package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging}
import com.github.unknownnpc.debugtesttool.connection.{Connection, VmJdiConnection}
import com.github.unknownnpc.debugtesttool.domain.{TestCase, TestTarget}
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JdiVmConnectionActor(testTarget: TestTarget)
                          (implicit executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  var jdiVmConnection: Connection = _

  override def preStart() {
    jdiVmConnection = VmJdiConnection(testTarget.address, testTarget.port)
  }

  override def receive = {

    case tc: TestCase =>
      jdiVmConnection.executeCommand(tc).onComplete {

        case Success(result) => sender ! JdiVmConnectionSuccess(result)

        case Failure(t) =>
          log.error(s"Actor failed test case execution: [${self.path}]")
          sender ! JdiVmConnectionFailed(t.getMessage)
      }

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.error(errorMessage)
      sender ! JdiVmConnectionFailed(errorMessage)

  }

}
