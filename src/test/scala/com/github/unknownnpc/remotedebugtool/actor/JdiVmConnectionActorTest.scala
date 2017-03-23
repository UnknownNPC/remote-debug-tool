package com.github.unknownnpc.remotedebugtool.actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.config.AppConfig
import com.github.unknownnpc.remotedebugtool.connection.VmConnection
import com.github.unknownnpc.remotedebugtool.domain.SystemConfig
import com.github.unknownnpc.remotedebugtool.report.ReportFormatter
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global

class JdiVmConnectionActorTest extends TestKit(ActorSystem("JdiVmConnectionActorTest"))
  with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with MockFactory {

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

  }

  private def jdiVmConnectionActorTestSample(vmConnection: VmConnection) = {
    TestActorRef(Props(new JdiVmConnectionActor(vmConnection) with AppConfig {
      override val servers = List.empty
      override val breakpoints = List.empty
      override val systemConfig = SystemConfig(Timeout(5, TimeUnit.MINUTES), Timeout(5, TimeUnit.MINUTES), mock[ReportFormatter])
    }))
  }

}
