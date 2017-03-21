package com.github.unknownnpc.debugtesttool.domain


sealed trait CaseSummary {

  def targetAddress: Address

  def targetPort: Port

  def breakPointLine: BreakpointLine

  def breakPointClassName: BreakpointClassName

  def resultPayload: ResultPayload

}

sealed trait ResultPayload {

  def targetId: ID

  def executionResult: CommandExecutionResult

}

case class JvmResultPayload(targetId: ID, executionResult: CommandExecutionResult) extends ResultPayload

case class JvmCaseSummary(targetAddress: Address,
                          targetPort: Port,
                          breakPointLine: BreakpointLine,
                          breakPointClassName: BreakpointClassName,
                          resultPayload: JvmResultPayload) extends CaseSummary
