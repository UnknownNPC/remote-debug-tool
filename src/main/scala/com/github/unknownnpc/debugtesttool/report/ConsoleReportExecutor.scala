package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.domain.{Address, CaseSummary, ID, Port}

trait ConsoleReportExecutor extends ReportExecutor {

  //not sure that it's ok : [
  override type T = String

  override def execute(summaries: List[CaseSummary]): String = {
    val converted: Seq[Seq[String]] = convertSummaryToSeqOfSeq(summaries)
    format(converted)
  }

  private def convertSummaryToSeqOfSeq(summaries: List[CaseSummary]): Seq[Seq[String]] = {
    val caseToSummaries: Map[(ID, Address, Port), List[CaseSummary]] = summaries.groupBy(
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

  private def format(table: Seq[Seq[String]]): String = table match {
    case Seq() => ""
    case _ =>
      val sizes = for (row <- table) yield for (cell <- row) yield if (cell == null) 0 else cell.toString.length
      val colSizes = for (col <- sizes.transpose) yield col.max
      val rows = for (row <- table) yield formatRow(row, colSizes)
      formatRows(rowSeparator(colSizes), rows)
  }

  private def headers(): Seq[String] = {
    Seq(
      "ID", "Address", "Port", "Class name", "Breakpoint line", "JVM value"
    )
  }

  private def formatRows(rowSeparator: String, rows: Seq[String]): String = (
    rowSeparator ::
      rows.head ::
      rows.tail.toList :::
      rowSeparator ::
      List()).mkString("\n")

  private def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
    val cells = for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else ("%" + size + "s").format(item)
    cells.mkString("|", "|", "|")
  }

  private def rowSeparator(colSizes: Seq[Int]) = colSizes map {
    "-" * _
  } mkString("+", "+", "+")

}

object ConsoleReportExecutor extends ConsoleReportExecutor
