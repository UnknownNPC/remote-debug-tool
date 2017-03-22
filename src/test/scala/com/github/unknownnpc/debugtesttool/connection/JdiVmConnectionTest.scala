package com.github.unknownnpc.debugtesttool.connection

import java.util.concurrent.TimeUnit

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
      val argsTask = JvmTestCase(1, 7, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "args")
      val result = executeTestCase(jdiConnection, argsTask)
      result.get should equal("\"someArgs\"")
    }

    "find int `two` inside `calculate` method at line `15`" in {
      val argsTask = JvmTestCase(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "two")
      val result = executeTestCase(jdiConnection, argsTask)
      result.get should equal("2")
    }

    "not find int `three` inside `calculate` method at line `15`" in {
      val argsTask = JvmTestCase(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three")
      val result = executeTestCase(jdiConnection, argsTask)
      result shouldBe None
    }

    "find int `three` inside `calculate` method at line `15`" in {
      val argsTask = JvmTestCase(1, 16, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three")
      val result = executeTestCase(jdiConnection, argsTask)
      result.get.toInt should be > 3
    }

  }

  private def executeTestCase(connection: VmConnection, testCase: JvmTestCase) = {
    connection.connect()
    connection.lockVm()
    connection.enableBreakpoint(testCase.breakPointLine, testCase.breakPointClassName)
    connection.unlockVm()
    val value = connection.findValue(testCase.fieldName, testCase.breakpointEventTriggerTimeout)
    connection.disableBreakpoint()
    connection.disconnect()
    value
  }

}
