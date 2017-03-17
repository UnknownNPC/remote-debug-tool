package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props}
import com.github.unknownnpc.debugtesttool.connection.{Connection, JdiVmConnection}
import com.github.unknownnpc.debugtesttool.domain.{BreakpointWaiting, TestCase, TestTarget}
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JdiVmConnectionActor(testTarget: TestTarget)(implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  private var jdiVmConnection: Connection = _

  override def preStart() {
    jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)
    jdiVmConnection.lockVm()
  }

  override def receive = {

    case request: JdiVmConnectionRequest =>
      val senderActor = sender()
      val testCase = request.testCase
      prepareVmForSearch(testCase)
      waitForBreakpoint(request.testCase.breakpointWaiting)
      val possibleResult = jdiVmConnection.findValue(testCase.breakPointThreadName, testCase.fieldName)
      possibleResult match {

        case Success(result) =>
          log.info(s"Connection received data from VM: [$result]")
          senderActor ! JdiVmConnectionSuccess(result)

        case Failure(t) =>
          val errorMessage: String = s"Failed test case execution for next case [${request.testCase}]"
          log.error(errorMessage)
          senderActor ! JdiVmConnectionFailed(errorMessage)
      }
      resetVM()

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)

  }

  private def waitForBreakpoint(breakpointWaiting: BreakpointWaiting) = {
    log.info("Implement sleep")
  }

  private def prepareVmForSearch(testCase: TestCase) {
    jdiVmConnection.setBreakpoint(testCase.breakPointLine, testCase.breakPointClassName)
    jdiVmConnection.unlockVm()
  }

  private def resetVM() = {
    jdiVmConnection.removeBreakpoint()
    jdiVmConnection.lockVm()
  }

}

object JdiVmConnectionActor {
  def props(testTarget: TestTarget)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(testTarget))
}
