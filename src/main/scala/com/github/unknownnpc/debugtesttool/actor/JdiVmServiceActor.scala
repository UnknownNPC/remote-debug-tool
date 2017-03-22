package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.config.AppConfig
import com.github.unknownnpc.debugtesttool.domain.{ExecutionPayload, _}
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

      log.info(s"Trying to distribute tasks for targets. Timeout: [${timeout.duration}]")
      val collectedPayload= appConfig.testCases.map { testCase =>

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
      pipeToReportActor(collectedPayload)

    case JdiVmServiceStop =>
      log.warning("VM resources cleaning")
      context.stop(self)

  }

  private def pipeToReportActor(allExecutionPayload: List[Future[ExecutionPayload]]) = {
    Future.sequence(allExecutionPayload).onComplete {
      case Success(executionPayloads) =>
        log.debug("Success cases were collected. Creating report")
        reportActorRef ! ReportServicePayload(executionPayloads.map(_.toReportRow))
      case Failure(reason) =>
        log.error(
          "Unable to prepare report. Payload collection process failed. " +
          "Please check logs and stop app"
        )
    }
  }

  private def connectionGatewayActorRef(testTargets: List[TestTarget]) = {
    context.actorOf(JdiVmGatewayActor.props(testTargets), "jdi-vm-gateway")
  }
}

object JdiVmServiceActor {
  def props(appConfig: AppConfig, reportActorRef: ActorRef)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmServiceActor(appConfig, reportActorRef))
}
