package com.github.unknownnpc.debugtesttool.connection

import java.util.concurrent.TimeUnit

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmTestCase
import com.github.unknownnpc.debugtesttool.util.JdkDebugProcessUtil
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.sys.process.Process

class JdiVmConnectionTest extends WordSpec
  with Matchers
  with BeforeAndAfterAll
  with JdkDebugProcessUtil {

  var aClassProcess: Process = _
  var jdiConnection: VmConnection = _

  override def beforeAll() {
    aClassProcess = runJavaClassInDebugMode("Atest", 8787, "someArgs")
    if (aClassProcess.isAlive())
      jdiConnection = JdiVmConnection("localhost", 8787)
  }

  override def afterAll() {
    aClassProcess.destroy()
  }

  "VmJdiConnection" should {

    "find array `args` inside `main` method at line `7`" in {
      val argsTask = JvmTestCase(1, 7, "Atest", FiniteDuration(10, TimeUnit.SECONDS), "args", NotNull)
      val result = executeTestCase(jdiConnection, argsTask)
      result.get should equal("\"someArgs\"")
    }

    "find int `two` inside `calculate` method at line `14`" in {
      val argsTask = JvmTestCase(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "two", NotNull)
      val result = executeTestCase(jdiConnection, argsTask)
      result.get should equal("2")
    }

    "not find int `three` inside `calculate` method at line `14`" in {
      val argsTask = JvmTestCase(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three", NotNull)
      val result = executeTestCase(jdiConnection, argsTask)
      result shouldBe None
    }

    "find int `three` inside `calculate` method at line `15`" in {
      val argsTask = JvmTestCase(1, 16, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three", NotNull)
      val result = executeTestCase(jdiConnection, argsTask)
      result.get.toInt should be > 3
    }

  }

  private def executeTestCase(connection: VmConnection, testCase: JvmTestCase) = {
    connection.lockVm()
    connection.setBreakpoint(testCase.breakPointLine, testCase.breakPointClassName)
    connection.unlockVm()
    val value = connection.findValue(testCase.breakPointThreadName, testCase.fieldName, testCase.breakpointWaiting)
    connection.removeBreakpoint()
    value
  }

}
