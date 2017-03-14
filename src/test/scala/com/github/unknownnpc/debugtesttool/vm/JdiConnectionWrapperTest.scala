package com.github.unknownnpc.debugtesttool.vm

import com.github.unknownnpc.debugtesttool.action.NotNull
import com.github.unknownnpc.debugtesttool.domain.JvmDebugInfo
import org.scalatest.FlatSpec

class JdiConnectionWrapperTest extends FlatSpec {

  "JdiConnectionWrapper" should "find values by DebugInfo" in {
    val vmWrapper = JdiConnectionWrapper("localhost", 8787)
    val info1: JvmDebugInfo = JvmDebugInfo(1, 5, "main","A", "args", NotNull)
    var s = "blablabla"
    vmWrapper.executeCommand(info1)
  }

}
