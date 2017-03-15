package com.github.unknownnpc.debugtesttool

import akka.actor.{Props, _}
import akka.pattern.ask
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.actor.JdiVmGatewayActor
import com.github.unknownnpc.debugtesttool.config.DebugTestToolConfig
import com.github.unknownnpc.debugtesttool.domain.TestTarget
import org.slf4j.LoggerFactory

import scala.io.StdIn
import scala.language.postfixOps

object Main {

  val log = LoggerFactory.getLogger(this.getClass)
  implicit val actorSystem = ActorSystem("debug-test-tool")
  implicit val timeout: Timeout = DebugTestToolConfig.systemConfig.remoteVmRequestTimeout
  implicit val context = actorSystem.dispatcher

  def main(args: Array[String]) {

    val connectionGatewayActor = connectionGatewayActorRef(DebugTestToolConfig.testTargets)

    val values = DebugTestToolConfig.testCases.map { testInfo =>
      (testInfo.testAction, connectionGatewayActor ? testInfo)
    }

    log.info("received values from remote VMs: {}", values.size)

    userMessage()
  }

  def userMessage() = {
    println("ENTER to terminate")
    StdIn.readLine()
    actorSystem.terminate()
  }

  private def connectionGatewayActorRef(testTargets: List[TestTarget]) = {
    actorSystem.actorOf(Props(new JdiVmGatewayActor(testTargets)), name = "vm-jdi-connections-gateway")
  }

}
