package com.github.unknownnpc.remotedebugtool.actor

import java.util.concurrent.TimeUnit
import akka.actor.{ActorSystem, Props, ReceiveTimeout}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.config.AppConfig
import com.github.unknownnpc.remotedebugtool.connection.VmConnection
import com.github.unknownnpc.remotedebugtool.domain.{JvmBreakpoint, JvmBreakpointPayload, SystemConfig}
import com.github.unknownnpc.remotedebugtool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}
import com.github.unknownnpc.remotedebugtool.report.ReportFormatter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class JdiVmConnectionActorTest extends TestKit(ActorSystem("JdiVmConnectionActorTest"))
  with ImplicitSender with WordSpecLike with Matchers with MockFactory {

  "JdiVmConnectionActor" should {

    "connect and lock vm on pre-start stage" in {
      val vmConnection = mock[VmConnection]
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
      }
      jdiVmConnectionActorTestSample(vmConnection)
    }

    "disconnect and unlock vm if `stop` requires" in {
      val vmConnection = mock[VmConnection]
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
        vmConnection.unlockVm _ expects()
        vmConnection.disconnect _ expects()
      }
      val actor = jdiVmConnectionActorTestSample(vmConnection)
      actor.stop()
    }

    "return `JdiVmConnectionSuccess` as answer for valid `JdiVmConnectionRequest`" in {
      val vmConnection = mock[VmConnection]
      val breakpointLine = 1
      val className = "A"
      val fieldName = "field"
      val awaitDuration = Timeout.zero.duration
      val breakpointValue = "value"
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
        vmConnection.enableBreakpoint _ expects(breakpointLine, className)
        vmConnection.unlockVm _ expects()
        vmConnection.findValue _ expects(fieldName, awaitDuration) returning Some(breakpointValue)
        vmConnection.lockVm _ expects()
        vmConnection.disableBreakpoint _ expects()
      }
      val actor = jdiVmConnectionActorTestSample(vmConnection)
      val breakpoint = JvmBreakpoint(1, breakpointLine, className, awaitDuration, fieldName)
      actor ! JdiVmConnectionRequest(breakpoint)
      expectMsg(JdiVmConnectionSuccess(JvmBreakpointPayload(breakpoint, breakpointValue)))
    }

    "return `JdiVmConnectionFailed` as answer for invalid `JdiVmConnectionRequest`" in {
      val vmConnection = mock[VmConnection]
      val breakpointLine = 1
      val className = "A"
      val fieldName = "field"
      val awaitDuration = Timeout.zero.duration
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
        vmConnection.enableBreakpoint _ expects(breakpointLine, className)
        vmConnection.unlockVm _ expects()
        vmConnection.findValue _ expects(fieldName, awaitDuration) returning None
        vmConnection.lockVm _ expects()
        vmConnection.disableBreakpoint _ expects()
      }
      val actor = jdiVmConnectionActorTestSample(vmConnection)
      val breakpoint = JvmBreakpoint(1, breakpointLine, className, awaitDuration, fieldName)
      actor ! JdiVmConnectionRequest(breakpoint)
      val errorMessage = s"Unable to find value for next test case: [$breakpoint]"
      expectMsg(JdiVmConnectionFailed(errorMessage))
    }

    "stop on `ReceiveTimeout` command" in {
      val vmConnection = mock[VmConnection]
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
        vmConnection.unlockVm _ expects()
        vmConnection.disconnect _ expects()
      }
      val actor = jdiVmConnectionActorTestSample(vmConnection)
      val testProbe = TestProbe()
      testProbe watch actor
      actor ! ReceiveTimeout
      testProbe.expectTerminated(actor)
    }

    "not crash if unknown message received" in {
      val vmConnection = mock[VmConnection]
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
      }
      val actor = jdiVmConnectionActorTestSample(vmConnection)
      val testProbe = TestProbe()
      testProbe watch actor
      actor ! "ouch"
      testProbe.expectNoMessage()
    }

    "release VM on timeout" in {
      val vmConnection = mock[VmConnection]
      inSequence {
        vmConnection.connect _ expects()
        vmConnection.lockVm _ expects()
        vmConnection.unlockVm _ expects()
        vmConnection.disconnect _ expects()
      }
      val actorIdleTimeout = Timeout(5, TimeUnit.SECONDS)
      val actor = jdiVmConnectionActorTestSample(vmConnection, actorIdleTimeout)
      val testProbe = TestProbe()
      testProbe watch actor
      testProbe.expectTerminated(actor, FiniteDuration(7, TimeUnit.SECONDS))
    }

  }

  private def jdiVmConnectionActorTestSample(vmConnection: VmConnection,
                                             remoteVmConnectionIdleTimeout: Timeout = Timeout(5, TimeUnit.MINUTES)) = {
    TestActorRef(Props(new JdiVmConnectionActor(vmConnection) with AppConfig {
      override val servers = List.empty
      override val breakpoints = List.empty
      override val systemConfig = SystemConfig(Timeout.zero, remoteVmConnectionIdleTimeout, mock[ReportFormatter])
    }))
  }

}
