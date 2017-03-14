package com.github.unknownnpc.debugtesttool.vm

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmDebugInfo
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class JdiConnectionWrapperTest extends WordSpec with Matchers {

  "JdiConnectionWrapper" should {

    /**
      * Right now test uses trivial code sample (test/resources/A.class)
      * and gets `args` values passed to the `main` method.
      * Compiled class `A` should be run via next command:
      * java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8787 A mahArgs
      *
      * TODO more exotic samples and `scala.sys.process` executor
      **/
    "find values" in {
      val jdiConnection = JdiConnectionWrapper("localhost", 8787)
      val jvmTask = JvmDebugInfo(1, 5, "main", "A", "args", NotNull)
      val variableValue = awaitFuture(jdiConnection.executeCommand(jvmTask))
      variableValue should equal("\"mahArgs\"")
    }
  }

  def awaitFuture[T](future: Future[T]): T = {
    Await.result(future, Duration.Inf)
  }

}
