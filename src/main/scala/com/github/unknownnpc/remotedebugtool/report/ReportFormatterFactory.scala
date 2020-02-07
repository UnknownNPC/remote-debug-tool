package com.github.unknownnpc.remotedebugtool.report

import com.github.unknownnpc.remotedebugtool.exception.ConfigException

trait ReportFormatterFactory {

  def findReportBy(reportType: ReportType.ReportTypeValue): ReportFormatter = {
    reportType match {

      case ReportType.ConsoleReport => ConsoleTableReportFormatter

      case _ => throw ConfigException("ReportType wasn't implemented")
    }
  }

  object ReportType extends Enumeration {
    type ReportTypeValue = Value
    val ConsoleReport = Value(CONSOLE_REPORT)
  }

}

object ReportFormatterFactory extends ReportFormatterFactory
