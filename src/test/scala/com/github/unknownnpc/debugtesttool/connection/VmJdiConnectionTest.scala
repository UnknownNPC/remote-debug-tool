package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmTestCase
import com.github.unknownnpc.debugtesttool.util.JdkDebugProcessUtil
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process.Process


class VmJdiConnectionTest extends WordSpec
  with Matchers
  with BeforeAndAfter
  with JdkDebugProcessUtil {

  lazy val logger = LoggerFactory.getLogger(getClass)
  var aClassProcess: Process = _

  before {
    aClassProcess = runJavaClassInDebugMode("AClass", "A", 8787, "passedArgs")
  }

  after {
    aClassProcess.destroy()
  }

  "VmJdiConnection" should {

    "find main method `args` values in the A.class test file" in {
      val jdiConnection = VmJdiConnection("localhost", 8787)
      val jvmTask = JvmTestCase(1, 5, "main", "A", "args", NotNull)
      val variableValue = awaitFuture(jdiConnection.executeCommand(jvmTask))
      variableValue should equal("\"passedArgs\"")
    }
  }

  def awaitFuture[T](future: Future[T]): T = {
    Await.result(future, Duration.Inf)
  }

}
