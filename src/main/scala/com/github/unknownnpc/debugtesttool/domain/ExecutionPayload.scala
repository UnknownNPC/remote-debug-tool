package com.github.unknownnpc.debugtesttool.domain

sealed trait ExecutionPayload {

  def testTarget: TestTarget

  def testCase: TestCase

  def testCaseValue: TestCaseValue

  def toReportRow: ReportRow

}

case class JvmExecutionPayload(testTarget: TestTarget,
                               testCase: TestCase,
                               testCaseValue: TestCaseValue) extends ExecutionPayload {

  override def toReportRow: ReportRow = {
    JvmReportRow(testTarget.id,
      testTarget.address,
      testTarget.port,
      testCase.breakPointLine,
      testCase.breakPointClassName,
      testCaseValue
    )
  }

}
