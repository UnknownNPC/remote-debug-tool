package com.github.unknownnpc.remotedebugtool.message

import com.github.unknownnpc.remotedebugtool.domain._

sealed trait Message

trait MainAppActorCommand extends Message
object MainAppActorStart extends MainAppActorCommand
object MainAppActorStop extends MainAppActorCommand

trait JdiVmServiceCommand extends Message
object JdiVmServiceStart extends JdiVmServiceCommand
object JdiVmServiceStop extends JdiVmServiceCommand

trait ReportServiceCommand extends Message
case class ReportServicePayload(executionPayload: BreakpointPayload) extends ReportServiceCommand
object ReportServicePrint extends ReportServiceCommand
object ReportServiceStop extends ReportServiceCommand

trait VmConnectionMessage extends Message
case class JdiVmConnectionRequest(breakpoint: Breakpoint) extends VmConnectionMessage
case class JdiVmConnectionSuccess(breakpointPayload: BreakpointPayload) extends VmConnectionMessage
case class JdiVmConnectionFailed(reason: CommandFailReason) extends VmConnectionMessage





