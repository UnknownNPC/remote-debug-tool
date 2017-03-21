package com.github.unknownnpc.debugtesttool.report

import com.github.unknownnpc.debugtesttool.domain.CaseSummary

trait ReportExecutor {

  type T

  def execute(summaries: List[CaseSummary]): T

}
