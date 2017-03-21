package com.github.unknownnpc.debugtesttool.domain


sealed trait CaseSummary {

  def targetId: ID

  def targetAddress: Address

  def targetPort: Port

  def breakPointLine: BreakpointLine

  def breakPointClassName: BreakpointClassName

  def executionResult: CommandExecutionResult

}

case class JvmCaseSummary(targetId: ID,
                          targetAddress: Address,
                          targetPort: Port,
                          breakPointLine: BreakpointLine,
                          breakPointClassName: BreakpointClassName,
                          executionResult: CommandExecutionResult) extends CaseSummary
