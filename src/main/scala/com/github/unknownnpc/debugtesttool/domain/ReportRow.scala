package com.github.unknownnpc.debugtesttool.domain

sealed trait ReportRow {
  def targetId: ID
  def targetAddress: Address
  def targetPort: Port
  def breakPointLine: BreakpointLine
  def breakPointClassName: BreakpointClassName
  def executionResult: TestCaseValue
}

case class JvmReportRow(targetId: ID,
                        targetAddress: Address,
                        targetPort: Port,
                        breakPointLine: BreakpointLine,
                        breakPointClassName: BreakpointClassName,
                        executionResult: TestCaseValue) extends ReportRow
