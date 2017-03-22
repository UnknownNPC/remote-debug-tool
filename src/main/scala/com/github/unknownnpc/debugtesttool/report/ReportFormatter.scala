package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.domain._

trait ReportFormatter {

  def format(rows: List[ReportRow]): String

  def convertRowsToSeqs(reportRows: List[ReportRow]): Seq[Seq[String]] = {
    val caseToSummaries: Map[(ID, Address, Port), List[ReportRow]] = reportRows.groupBy(
      record => (record.targetId, record.targetAddress, record.targetPort)
    )
    headers() +:
      caseToSummaries.flatMap { case (key, rows) =>
        Seq(s"${key._1}", s"${key._2}", s"${key._3}", "", "", "") ::
          rows.map { row =>
            Seq("", "", "", s"${row.breakPointClassName}", s"${row.breakPointLine}", s"${row.executionResult}")
          }
      }.toSeq
  }

  private def headers() = {
    Seq(
      "ID", "Address", "Port", "Class name", "Breakpoint line", "JVM value"
    )
  }

}
