package com.github.unknownnpc.remotedebugtool.domain

import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.report.ReportFormatter

case class SystemConfig(remoteVmRequestTimeout: Timeout,
                        remoteVmConnectionIdleTimeout: Timeout,
                        reportFormatter: ReportFormatter)
