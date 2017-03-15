package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmTestCase
import com.github.unknownnpc.debugtesttool.util.VmJdiTestUtil
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.io.File
import scala.sys.process.Process


class VmJdiConnectionTest extends WordSpec with Matchers with BeforeAndAfter with VmJdiTestUtil {

  var aClassProcess: Process = _

  before {
    aClassProcess = runJdiJavaClass(File.separator + "AClass" + File.separator, "A", 8787, "passedArgs")
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
