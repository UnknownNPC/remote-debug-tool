package com.github.unknownnpc.remotedebugtool.report

import com.github.unknownnpc.remotedebugtool.domain.ReportRow

trait ConsoleTableReportFormatter extends ReportFormatter {

  override def format(rows: List[ReportRow]): String = {
    val converted: Seq[Seq[String]] = convertRowsToSeqs(rows)
    format(converted)
  }

  private def format(table: Seq[Seq[String]]): String = table match {
    case Seq() => ""
    case _ =>
      val sizes = for (row <- table) yield for (cell <- row) yield if (cell == null) 0 else cell.toString.length
      val colSizes = for (col <- sizes.transpose) yield col.max
      val rows = for (row <- table) yield formatRow(row, colSizes)
      formatRows(rowSeparator(colSizes), rows)
  }

  private def formatRows(rowSeparator: String, rows: Seq[String]): String = (
    "\n" ::
      rowSeparator ::
      rows.head ::
      rows.tail.toList :::
      rowSeparator ::
      "\n" ::
      List()).mkString("\n")

  private def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
    val cells = for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else ("%" + size + "s").format(item)
    cells.mkString("|", "|", "|")
  }

  private def rowSeparator(colSizes: Seq[Int]) = colSizes map {
    "-" * _
  } mkString("+", "+", "+")

}

object ConsoleTableReportFormatter extends ConsoleTableReportFormatter
