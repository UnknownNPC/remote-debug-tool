package com.github.unknownnpc.debugtesttool

import scala.util.{Failure, Success}
import akka.actor.{Props, _}
import akka.pattern.ask
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.actor.JdiVmGatewayActor
import com.github.unknownnpc.debugtesttool.config.DebugTestToolConfig
import com.github.unknownnpc.debugtesttool.domain.TestTarget
import com.github.unknownnpc.debugtesttool.message.{ConnectionGatewayFailed, ConnectionGatewayPayload, ConnectionGatewaySuccess, GatewayMessage}
import org.slf4j.LoggerFactory

import scala.io.StdIn
import scala.language.postfixOps

object Main {

  private val log = LoggerFactory.getLogger(this.getClass)
  private implicit val actorSystem = ActorSystem("debug-test-tool")
  private implicit val timeout: Timeout = DebugTestToolConfig.systemConfig.remoteVmRequestTimeout
  private implicit val context = actorSystem.dispatcher

  def main(args: Array[String]) {

    val connectionGatewayActor = connectionGatewayActorRef(DebugTestToolConfig.testTargets)

    DebugTestToolConfig.testCases.foreach { testInfo =>

      (connectionGatewayActor ? ConnectionGatewayPayload(testInfo)).onComplete {
        case Success(futureResult) => futureResult match {
          case ConnectionGatewaySuccess(resultPayload) => log.info(s"Received next payload [$resultPayload] using next [$testInfo]")
          case ConnectionGatewayFailed(reason) => log.info(s"Gateway returned failed result  [$reason]")
          case _ => log.error(s"Received unknown message from: [${connectionGatewayActor.toString()}]")
        }

        case Failure(t) =>
          log.error(s"Actor failed test case execution: [${connectionGatewayActor.path}]")
      }
    }

    userMessage()
  }

  private def userMessage() = {
    println("ENTER to terminate")
    StdIn.readLine()
    actorSystem.terminate()
  }

  private def connectionGatewayActorRef(testTargets: List[TestTarget]) = {
    actorSystem.actorOf(Props(new JdiVmGatewayActor(testTargets)), name = "vm-jdi-connections-gateway")
  }

}
