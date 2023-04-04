package com.github.unknownnpc.remotedebugtool.actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class JdiVmGatewayActorTest extends TestKit(ActorSystem("JdiVmGatewayActorTest"))
  with ImplicitSender with DefaultTimeout with WordSpecLike with Matchers with MockFactory with BeforeAndAfter {

  var targetProbes: mutable.Map[ID, TestProbe] = _

  before {
    targetProbes = mutable.Map.empty[ID, TestProbe]
  }

  "JdiVmGatewayActorTest" should {

    "redirect `JdiVmConnectionRequest` to connection actor if connection exists and pipe result to sender" in {
      val firstId = 1
      val timeout = Timeout(10, TimeUnit.SECONDS).duration
      val servers = List(JvmTarget(firstId, "localhost", 8080))
      val breakpoint = JvmBreakpoint(firstId, 13, "A", timeout, "field")
      val testActorRef = jdiVmGatewayActorTestSample(servers)
      val request = JdiVmConnectionRequest(breakpoint)
      val success = JdiVmConnectionSuccess(JvmBreakpointPayload(breakpoint, "something"))
      testActorRef ! request
      targetProbes(firstId).expectMsg(request)
      targetProbes(firstId).reply(success)
      expectMsg(success)
    }

    "return `JdiVmConnectionFailed` to sender if connection doesn't exist" in {
      val incorrectTarget = 99
      val timeout = Timeout(10, TimeUnit.SECONDS).duration
      val servers = List.empty
      val testActorRef = jdiVmGatewayActorTestSample(servers)
      val breakpoint = JvmBreakpoint(incorrectTarget, 13, "A", timeout, "field")
      val request = JdiVmConnectionRequest(breakpoint)
      testActorRef ! request
      val failMessage = JdiVmConnectionFailed(s"Unable to find target server id: [$incorrectTarget]")
      expectMsg(failMessage)
    }

    "not fail if unknown message received" in {
      val servers = List.empty
      val actor = jdiVmGatewayActorTestSample(servers)
      val testProbe = TestProbe()
      testProbe watch actor
      actor ! "ouch"
      testProbe.expectNoMessage()
    }

  }

  private def jdiVmGatewayActorTestSample(testTargets: List[Target]) = {
    TestActorRef(Props(new JdiVmGatewayActor(testTargets) {
      override def targetToConnection(testTarget: Target): (ID, ActorRef) = {
        val connectionPair = testTarget.id -> TestProbe()
        targetProbes += connectionPair
        (connectionPair._1, connectionPair._2.ref)
      }
    }))
  }

}
