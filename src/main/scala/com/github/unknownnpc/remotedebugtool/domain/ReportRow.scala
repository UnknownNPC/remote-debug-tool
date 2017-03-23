package com.github.unknownnpc.remotedebugtool.domain

sealed trait ReportRow {
  def targetId: ID
  def targetAddress: Address
  def targetPort: Port
  def breakPointLine: BreakpointLine
  def breakPointClassName: BreakpointClassName
  def executionResult: BreakpointValue
}

case class JvmReportRow(targetId: ID,
                        targetAddress: Address,
                        targetPort: Port,
                        breakPointLine: BreakpointLine,
                        breakPointClassName: BreakpointClassName,
                        executionResult: BreakpointValue) extends ReportRow
