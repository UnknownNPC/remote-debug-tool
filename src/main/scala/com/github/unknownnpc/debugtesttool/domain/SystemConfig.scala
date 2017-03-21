package com.github.unknownnpc.debugtesttool.domain

import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.report.ReportExecutor

case class SystemConfig(remoteVmRequestTimeout: Timeout,
                        removeVmConnectionIdleTimeout: Timeout,
                        reportExecutor: ReportExecutor)
