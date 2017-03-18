package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import com.github.unknownnpc.debugtesttool.config.DebugTestToolConfig
import com.github.unknownnpc.debugtesttool.connection.{JdiVmConnection, VmConnection}
import com.github.unknownnpc.debugtesttool.domain.{TestCase, TestTarget}
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext

class JdiVmConnectionActor(testTarget: TestTarget)(implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val actorIdleTimeout = DebugTestToolConfig.systemConfig.removeVmConnectionIdleTimeout.duration
  private var jdiVmConnection: VmConnection = _

  override def preStart() {
    context.setReceiveTimeout(actorIdleTimeout)
    jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)
    jdiVmConnection.lockVm()
  }


  override def receive = {

    case request: JdiVmConnectionRequest =>
      val senderActor = sender()
      val testCase = request.testCase
      prepareVmForSearch(testCase)
      val possibleResult = jdiVmConnection.findValue(
        testCase.breakPointThreadName, testCase.fieldName, testCase.breakpointWaiting
      )

      possibleResult match {

        case Some(result) =>
          log.info(s"Connection received data from VM: [$result]")
          senderActor ! JdiVmConnectionSuccess(result)

        case None =>
          val errorMessage: String = s"Unable to find value for next test case [${request.testCase}]"
          log.error(errorMessage)
          senderActor ! JdiVmConnectionFailed(errorMessage)
      }
      resetVM()

    case ReceiveTimeout =>
      log.info(s"Connection actor is idling after [$actorIdleTimeout]. Unlock VM. Stopping...")
      jdiVmConnection.unlockVm()
      context.stop(self)

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)

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
