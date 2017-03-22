package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import com.github.unknownnpc.debugtesttool.config.{AppConfig, DebugTestToolConfig}
import com.github.unknownnpc.debugtesttool.connection.VmConnection
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext

class JdiVmConnectionActor(vmConnection: VmConnection)(implicit executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  self: AppConfig =>

  override def preStart() {
    context.setReceiveTimeout(systemConfig.removeVmConnectionIdleTimeout.duration)
    log.debug("Pre-start: connect and lock VM")
    vmConnection.connect()
    vmConnection.lockVm()
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop() {
    log.debug("Post-stop: unlock and disconnect VM")
    vmConnection.unlockVm()
    vmConnection.disconnect()
  }

  override def receive = {

    case JdiVmConnectionRequest(testCase)  =>
      val senderActor = sender()
      syncBreakpointEnableWithVm(testCase.breakPointLine, testCase.breakPointClassName)
      val optionalValue = vmConnection.findValue(testCase.fieldName, testCase.breakpointEventTriggerTimeout)

      optionalValue match {

        case Some(value) =>
          log.info(s"Connection received data from VM: [$value]")
          senderActor ! JdiVmConnectionSuccess(JvmExecutionPayload(testCase, value))

        case None =>
          val errorMessage: String = s"Unable to find value for next test case: [${testCase}]"
          log.error(errorMessage)
          senderActor ! JdiVmConnectionFailed(errorMessage)
      }
      syncBreakpointDisableWithVm()

    case ReceiveTimeout =>
      log.info(s"Connection actor is idling after [$systemConfig.removeVmConnectionIdleTimeout.duration]. Unlock VM. Stopping.")
      context.stop(context.self)

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)

  }

  private def syncBreakpointEnableWithVm(breakPointLine: BreakpointLine, breakpointClassName: BreakpointClassName) {
    log.debug("Replacing `VM lock` with `Breakpoint enable`")
    vmConnection.enableBreakpoint(breakPointLine, breakpointClassName)
    vmConnection.unlockVm()
  }

  private def syncBreakpointDisableWithVm() = {
    log.debug("Replacing `Breakpoint enable` with `VM lock`")
    vmConnection.lockVm()
    vmConnection.disableBreakpoint()
  }

}

object JdiVmConnectionActor {
  def props(vmConnection: VmConnection)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(vmConnection) with DebugTestToolConfig)
}
