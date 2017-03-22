package com.github.unknownnpc.debugtesttool.domain

import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.report.ReportFormatter

case class SystemConfig(remoteVmRequestTimeout: Timeout,
                        removeVmConnectionIdleTimeout: Timeout,
                        reportFormatter: ReportFormatter)
