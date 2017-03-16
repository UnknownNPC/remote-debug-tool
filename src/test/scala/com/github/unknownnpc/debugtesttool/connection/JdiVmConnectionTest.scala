package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.{CommandExecutionResult, JvmTestCase}
import com.github.unknownnpc.debugtesttool.util.JdkDebugProcessUtil
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.duration.Duration
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
      val jvmTask = JvmTestCase(1, 5, "main", "A", "args", NotNull)
      jdiConnection.lockVm()
      jdiConnection.setBreakpoint(jvmTask.breakPointLine, jvmTask.breakPointClassName)
      jdiConnection.unlockVm()
      val variableValue: Future[CommandExecutionResult] = jdiConnection.findValue(jvmTask)
      jdiConnection.removeBreakpoint()
      awaitFuture(variableValue) should equal("\"passedArgs\"")
    }

    "find variable values in the B.class test file by lines" in {
      val jdiConnection = JdiVmConnection("localhost", 8788)
      val jvmTaskForBValue = JvmTestCase(1, 4, "main", "B", "b", NotNull)
      val jvmTaskForCValue = JvmTestCase(1, 5, "main", "B", "c", NotNull)

      jdiConnection.lockVm()
      jdiConnection.setBreakpoint(jvmTaskForBValue.breakPointLine, jvmTaskForBValue.breakPointClassName)
      jdiConnection.unlockVm()
      val variableValueB: Future[CommandExecutionResult] = jdiConnection.findValue(jvmTaskForBValue)
      jdiConnection.removeBreakpoint()

      jdiConnection.lockVm()
      jdiConnection.setBreakpoint(jvmTaskForCValue.breakPointLine, jvmTaskForCValue.breakPointClassName)
      jdiConnection.unlockVm()
      val variableValueC: Future[CommandExecutionResult] = jdiConnection.findValue(jvmTaskForCValue)
      jdiConnection.removeBreakpoint()

      val bValue = awaitFuture(variableValueB)
      val cValue = awaitFuture(variableValueC)

      bValue should equal("47")
      cValue should equal("stringSample")
    }
  }

  def awaitFuture[T](future: Future[T]): T = {
    Await.result(future, Duration.Inf)
  }

}
