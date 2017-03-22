package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import com.github.unknownnpc.debugtesttool.config.DebugTestToolConfig
import com.github.unknownnpc.debugtesttool.connection.JdiVmConnection
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext

class JdiVmConnectionActor(testTarget: TestTarget)(implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val actorIdleTimeout = DebugTestToolConfig.systemConfig.removeVmConnectionIdleTimeout.duration
  private val jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)

  override def preStart() {
    context.setReceiveTimeout(actorIdleTimeout)
    log.debug("Pre-start: connect and lock VM")
    jdiVmConnection.connect()
    jdiVmConnection.lockVm()
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop() {
    log.debug("Post-stop: unlock and disconnect VM")
    jdiVmConnection.unlockVm()
    jdiVmConnection.disconnect()
  }

  override def receive = {

    case request: JdiVmConnectionRequest =>
      val senderActor = sender()
      val testCase = request.testCase
      syncBreakpointEnableWithVm(testCase.breakPointLine, testCase.breakPointClassName)
      val optionalValue = jdiVmConnection.findValue(testCase.fieldName, testCase.breakpointEventTriggerTimeout)

      optionalValue match {

        case Some(value) =>
          log.info(s"Connection received data from VM: [$value]")
          senderActor ! JdiVmConnectionSuccess(JvmExecutionPayload(testTarget, testCase, value))

        case None =>
          val errorMessage: String = s"Unable to find value for next test case: [${request.testCase}]"
          log.error(errorMessage)
          senderActor ! JdiVmConnectionFailed(errorMessage)
      }
      syncBreakpointDisableWithVm()

    case ReceiveTimeout =>
      log.info(s"Connection actor is idling after [$actorIdleTimeout]. Unlock VM. Stopping.")
      context.stop(self)

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)

  }

  private def syncBreakpointEnableWithVm(breakPointLine: BreakpointLine, breakpointClassName: BreakpointClassName) {
    log.debug("Replacing `VM lock` with `Breakpoint enable`")
    jdiVmConnection.enableBreakpoint(breakPointLine, breakpointClassName)
    jdiVmConnection.unlockVm()
  }

  private def syncBreakpointDisableWithVm() = {
    log.debug("Replacing `Breakpoint enable` with `VM lock`")
    jdiVmConnection.lockVm()
    jdiVmConnection.disableBreakpoint()
  }

}

object JdiVmConnectionActor {
  def props(testTarget: TestTarget)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(testTarget))
}
