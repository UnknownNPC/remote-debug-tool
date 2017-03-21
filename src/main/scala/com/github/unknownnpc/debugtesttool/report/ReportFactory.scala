package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.exception.ConfigException

trait ReportFactory {

  def findReportBy(reportType: ReportType.ReportTypeValue): ReportExecutor = {
    reportType match {
      case ReportType.ConsoleReport => ConsoleReportExecutor
      case _ => throw ConfigException("ReportType wasn't implemented")
    }
  }

  object ReportType extends Enumeration {
    type ReportTypeValue = Value
    val ConsoleReport = Value(CONSOLE_REPORT)
    val FileReport = Value(FILE_REPORT)
  }

}

object ReportFactory extends ReportFactory
