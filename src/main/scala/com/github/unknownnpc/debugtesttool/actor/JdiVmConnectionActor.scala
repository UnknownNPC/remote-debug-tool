package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.github.unknownnpc.debugtesttool.connection.{Connection, JdiVmConnection}
import com.github.unknownnpc.debugtesttool.domain.TestTarget
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JdiVmConnectionActor(testTarget: TestTarget)
                          (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  private var jdiVmConnection: Connection = _

  override def preStart() {
    jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)
  }

  override def receive = {

    case request: JdiVmConnectionRequest =>
      val parentActor = sender()
      jdiVmConnection.executeCommand(request.testCase).onComplete {

        case Success(result) =>
          log.info(s"Connection received data from VM: [$result]")
          parentActor ! JdiVmConnectionSuccess(result)

        case Failure(t) =>
          val errorMessage: String = s"Actor [${self.path}] failed test case execution for next case [${request.testCase}]"
          log.error(errorMessage)
          parentActor ! JdiVmConnectionFailed(errorMessage)

      }

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)
  }

}

object JdiVmConnectionActor {
  def props(testTarget: TestTarget)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(testTarget))
}
