package com.github.unknownnpc.debugtesttool.connection

import java.util.concurrent.TimeUnit

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.{CommandExecutionResult, JvmTestCase}
import com.github.unknownnpc.debugtesttool.util.JdkDebugProcessUtil
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.process.Process

class JdiVmConnectionTest extends WordSpec
  with Matchers
  with BeforeAndAfter
  with JdkDebugProcessUtil {

  var aClassProcess: Process = _
  var bClassProcess: Process = _

  before {
    aClassProcess = runJavaClassInDebugMode("AClass", "A", 8787, "passedArgs")
    bClassProcess = runJavaClassInDebugMode("BClass", "B", 8788)
  }

  after {
    aClassProcess.destroy()
    bClassProcess.destroy()
  }

  "VmJdiConnection" should {

    "find main method `args` values in the A.class test file" in {
      val jdiConnection = JdiVmConnection("localhost", 8787)
      val jvmTask = JvmTestCase(1, 5, "main", "A", FiniteDuration(2, TimeUnit.SECONDS), "args", NotNull)
      val result = executeTestCase(jdiConnection, jvmTask)
      result should equal("\"passedArgs\"")
    }

    "find variable values in the B.class test file by lines" in {
      val jdiConnection = JdiVmConnection("localhost", 8788)
      val jvmTaskForBValue = JvmTestCase(1, 4, "main", "B", FiniteDuration(2, TimeUnit.SECONDS), "b", NotNull)
      val jvmTaskForCValue = JvmTestCase(1, 5, "main", "B", FiniteDuration(2, TimeUnit.SECONDS), "c", NotNull)
      val resultB = executeTestCase(jdiConnection, jvmTaskForBValue)
      val resultC = executeTestCase(jdiConnection, jvmTaskForCValue)
      resultB should equal("47")
      resultC should equal("stringSample")
    }
  }

  private def executeTestCase(connection: Connection, testCase: JvmTestCase) = {
    connection.lockVm()
    connection.setBreakpoint(testCase.breakPointLine, testCase.breakPointClassName)
    connection.unlockVm()
    val possibleValueB = connection.findValue(testCase.breakPointThreadName, testCase.fieldName)
    connection.removeBreakpoint()
    possibleValueB.get
  }

}
