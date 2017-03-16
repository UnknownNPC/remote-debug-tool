package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.config.AppConfig
import com.github.unknownnpc.debugtesttool.domain.TestTarget
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/*
  Context for onComplete()
 */
class JdiVmServiceActor(appConfig: AppConfig)(implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = appConfig.systemConfig.remoteVmRequestTimeout
  private val connectionGatewayActor = connectionGatewayActorRef(appConfig.testTargets)

  override def receive = {

    case JdiVmServiceStart =>
      appConfig.testCases.foreach { testCase =>
        (connectionGatewayActor ? JdiVmConnectionRequest(testCase)).onComplete {

          case Success(futureResult) => futureResult match {

            case JdiVmConnectionSuccess(resultPayload) => log.info(s"Received next payload [$resultPayload] using next [$testCase]")

            case JdiVmConnectionFailed(reason) => log.info(s"Gateway returned failed result:  [$reason]")

            case _ => log.error(s"Received unknown message from: [${connectionGatewayActor.toString()}]")

          }

          case Failure(t) =>
            log.error(s"Actor failed test case execution: [${connectionGatewayActor.path}]")
        }
      }

    case JdiVmServiceStop =>
      log.warning("VM resources cleaning")

  }

  private def connectionGatewayActorRef(testTargets: List[TestTarget]) = {
    context.actorOf(JdiVmGatewayActor.props(testTargets), "jdi-vm-gateway")
  }
}

object JdiVmServiceActor {
  def props(appConfig: AppConfig)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmServiceActor(appConfig))
}
