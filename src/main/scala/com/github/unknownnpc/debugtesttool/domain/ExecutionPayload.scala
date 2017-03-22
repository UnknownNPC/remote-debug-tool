package com.github.unknownnpc.debugtesttool.domain

sealed trait ExecutionPayload {
  def testCase: TestCase
  def testCaseValue: TestCaseValue
}

case class JvmExecutionPayload(testCase: TestCase,
                               testCaseValue: TestCaseValue) extends ExecutionPayload
