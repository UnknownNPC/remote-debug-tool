package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.domain.ReportRow

trait ReportExecutor {

  type T

  def execute(summaries: List[ReportRow]): T

}
