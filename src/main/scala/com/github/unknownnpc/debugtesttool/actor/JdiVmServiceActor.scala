package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.config.AppConfig
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/*
  Context for onComplete()
 */
class JdiVmServiceActor(appConfig: AppConfig, reportActorRef: ActorRef)(implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = appConfig.systemConfig.remoteVmRequestTimeout
  private val connectionGatewayActor = connectionGatewayActorRef(appConfig.testTargets)

  override def receive = {

    case JdiVmServiceStart =>
      val l = List[Future[CommandExecutionResult]]=
        appConfig.testCases.map { testCase =>

          (connectionGatewayActor ? JdiVmConnectionRequest(testCase)).flatMap {
          case JdiVmConnectionSuccess(resultPayload) =>
            log.info(s"Received next payload [$resultPayload] using next [$testCase]")
            Future.successful(resultPayload)

          case JdiVmConnectionFailed(reason) =>
            log.error(s"Gateway returned failed result:  [$reason]")
            Future.failed(VmException(reason))

          case _ =>
            val errorMessage: String = s"Received unknown message from: [${connectionGatewayActor.toString()}]"
            log.error(errorMessage)
            Future.failed(VmException(errorMessage))
        }
      }

    case JdiVmServiceStop =>
      log.warning("VM resources cleaning")
      context.stop(self)

  }

  private def createReportSummaryFrom(testCase: TestCase, testTarget: TestTarget, result: CommandExecutionResult) = {
    JvmCaseSummary(
      testTarget.id,
      testTarget.address,
      testTarget.port,
      testCase.breakPointLine,
      testCase.breakPointClassName,
      result
    )
  }

  private def connectionGatewayActorRef(testTargets: List[TestTarget]) = {
    context.actorOf(JdiVmGatewayActor.props(testTargets), "jdi-vm-gateway")
  }
}

object JdiVmServiceActor {
  def props(appConfig: AppConfig, reportActorRef: ActorRef)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmServiceActor(appConfig, reportActorRef))
}
