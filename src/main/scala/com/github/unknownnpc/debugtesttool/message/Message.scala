package com.github.unknownnpc.debugtesttool.message

import com.github.unknownnpc.debugtesttool.domain._

sealed trait Message

trait MainAppActorCommand extends Message
object MainAppActorStart extends MainAppActorCommand
object MainAppActorStop extends MainAppActorCommand

trait JdiVmServiceCommand extends Message
object JdiVmServiceStart extends JdiVmServiceCommand
object JdiVmServiceStop extends JdiVmServiceCommand

trait ReportServiceCommand extends Message
case class ReportServicePayload(summaries: List[CaseSummary]) extends ReportServiceCommand
object ReportServiceStop extends ReportServiceCommand

trait VmConnectionMessage extends Message
case class JdiVmConnectionRequest(testCase: TestCase) extends VmConnectionMessage
case class JdiVmConnectionSuccess(result: ResultPayload) extends VmConnectionMessage
case class JdiVmConnectionFailed(reason: CommandFailReason) extends VmConnectionMessage





