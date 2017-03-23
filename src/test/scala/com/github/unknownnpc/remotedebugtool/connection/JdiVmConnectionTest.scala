package com.github.unknownnpc.remotedebugtool.connection

import java.util.concurrent.TimeUnit

import com.github.unknownnpc.remotedebugtool.domain.JvmBreakpoint
import com.github.unknownnpc.remotedebugtool.util.JdkDebugProcessUtil
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.sys.process.Process

class JdiVmConnectionTest extends WordSpec
  with Matchers
  with BeforeAndAfter
  with JdkDebugProcessUtil {

  var aClassProcess: Process = _
  var jdiConnection: VmConnection = _

  before {
    aClassProcess = runJavaClassInDebugMode("Atest", 8787, "someArgs")
    sleepForFourSec()
    jdiConnection = JdiVmConnection("localhost", 8787)
  }

  after {
    aClassProcess.destroy()
    sleepForFourSec()
  }

  "VmJdiConnection" should {

    "find array `args` inside `main` method at line `7`" in {
      val breakpoint = JvmBreakpoint(1, 7, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "args")
      val result = executeBreakpoint(jdiConnection, breakpoint)
      result.get should equal("\"someArgs\"")
    }

    "find int `two` inside `calculate` method at line `15`" in {
      val breakpoint = JvmBreakpoint(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "two")
      val result = executeBreakpoint(jdiConnection, breakpoint)
      result.get should equal("2")
    }

    "not find int `three` inside `calculate` method at line `15`" in {
      val breakpoint = JvmBreakpoint(1, 15, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three")
      val result = executeBreakpoint(jdiConnection, breakpoint)
      result shouldBe None
    }

    "find int `three` inside `calculate` method at line `15`" in {
      val breakpoint = JvmBreakpoint(1, 16, "Atest", FiniteDuration(20, TimeUnit.SECONDS), "three")
      val result = executeBreakpoint(jdiConnection, breakpoint)
      result.get.toInt should be > 3
    }

  }

  //Weird. `TODO` possible to resolve it via `Eventually` trait
  private def sleepForFourSec(): Unit = {
    Thread.sleep(4000)
  }

  private def executeBreakpoint(connection: VmConnection, testCase: JvmBreakpoint) = {
    connection.connect()
    connection.lockVm()
    connection.enableBreakpoint(testCase.line, testCase.className)
    connection.unlockVm()
    val value = connection.findValue(testCase.fieldName, testCase.eventTriggerTimeout)
    connection.disableBreakpoint()
    connection.disconnect()
    value
  }

}
