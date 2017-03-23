package com.github.unknownnpc.remotedebugtool.actor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import com.github.unknownnpc.remotedebugtool.config.{AppConfig, RemoteDebugToolConfig}
import com.github.unknownnpc.remotedebugtool.connection.VmConnection
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.concurrent.ExecutionContext

class JdiVmConnectionActor(vmConnection: VmConnection)(implicit executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  self: AppConfig =>

  override def preStart() {
    context.setReceiveTimeout(systemConfig.remoteVmConnectionIdleTimeout.duration)
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

    case JdiVmConnectionRequest(breakpoint)  =>
      val senderActor = sender()
      syncVmBreakpointEnableWithVm(breakpoint.line, breakpoint.className)
      val optionalValue = vmConnection.findValue(breakpoint.fieldName, breakpoint.eventTriggerTimeout)

      optionalValue match {

        case Some(value) =>
          log.info(s"Connection received data from VM: [$value]")
          senderActor ! JdiVmConnectionSuccess(JvmBreakpointPayload(breakpoint, value))

        case None =>
          val errorMessage: String = s"Unable to find value for next test case: [${breakpoint}]"
          log.error(errorMessage)
          senderActor ! JdiVmConnectionFailed(errorMessage)
      }
      syncVmBreakpointDisableWithVm()

    case ReceiveTimeout =>
      log.info(s"Connection actor is idling after [${systemConfig.remoteVmConnectionIdleTimeout.duration}]. Unlock VM. Stopping.")
      context.stop(context.self)

    case _ =>
      val errorMessage = "Unknown incoming message"
      log.warning(errorMessage)

  }

  private def syncVmBreakpointEnableWithVm(breakpointLine: BreakpointLine, breakpointClassName: BreakpointClassName) {
    log.debug("Replacing `VM lock` with `Breakpoint enable`")
    vmConnection.enableBreakpoint(breakpointLine, breakpointClassName)
    vmConnection.unlockVm()
  }

  private def syncVmBreakpointDisableWithVm() = {
    log.debug("Replacing `Breakpoint enable` with `VM lock`")
    vmConnection.lockVm()
    vmConnection.disableBreakpoint()
  }

}

object JdiVmConnectionActor {
  def props(vmConnection: VmConnection)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(vmConnection) with RemoteDebugToolConfig)
}
