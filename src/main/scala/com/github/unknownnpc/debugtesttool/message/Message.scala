package com.github.unknownnpc.debugtesttool.message

import com.github.unknownnpc.debugtesttool.domain.{CommandExecutionResult, CommandFailReason, TestCase}

sealed trait Message
sealed trait GatewayMessage extends Message
sealed trait VmConnectionMessage extends Message

case class ConnectionGatewayPayload(testCase: TestCase) extends GatewayMessage
case class ConnectionGatewaySuccess(result: CommandExecutionResult) extends GatewayMessage
case class ConnectionGatewayFailed(reason: CommandFailReason)

case class JdiVmConnectionSuccess(result: CommandExecutionResult) extends VmConnectionMessage
case class JdiVmConnectionFailed(reason: CommandFailReason) extends VmConnectionMessage
