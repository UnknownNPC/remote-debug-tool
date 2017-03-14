package com.github.unknownnpc.debugtesttool.vm

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmDebugInfo
import org.scalatest.FlatSpec

class JdiConnectionWrapperTest extends FlatSpec {

  "JdiConnectionWrapper" should "find values by DebugInfo" in {
    val vmWrapper = JdiConnectionWrapper("localhost", 5005)
    val info1: JvmDebugInfo = JvmDebugInfo(1, 53, "main","com.datalex.tdp.soap.test.run.StandaloneTestRunner", "MILLISECONDS_IN_SECOND", NotNull)
    var s = "blablabla"
    vmWrapper.executeCommand(info1)
  }

}
