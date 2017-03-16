package com.github.unknownnpc.debugtesttool.message

import com.github.unknownnpc.debugtesttool.domain.{CommandExecutionResult, CommandFailReason, TestCase}

sealed trait Message

trait VmConnectionMessage extends Message

trait MainAppActorCommand extends Message

trait JdiVmServiceCommand extends Message


case class JdiVmConnectionRequest(testCase: TestCase) extends VmConnectionMessage

case class JdiVmConnectionSuccess(result: CommandExecutionResult) extends VmConnectionMessage

case class JdiVmConnectionFailed(reason: CommandFailReason) extends VmConnectionMessage

case object MainAppActorStart extends MainAppActorCommand

case object MainAppActorStop extends MainAppActorCommand

object JdiVmServiceStart extends JdiVmServiceCommand

object JdiVmServiceStop extends JdiVmServiceCommand
