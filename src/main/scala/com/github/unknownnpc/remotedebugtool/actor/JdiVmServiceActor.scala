package com.github.unknownnpc.remotedebugtool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.config.{AppConfig, RemoteDebugToolConfig}
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.message.{ReportServicePrint, _}

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

class JdiVmServiceActor(reportActorRef: ActorRef)(implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  self: AppConfig =>

  private val connectionGatewayActor = connectionGatewayActorRef(servers)
  private implicit lazy val remoteVmRequestTimeout: Timeout = systemConfig.remoteVmRequestTimeout

  override def receive = {

    case JdiVmServiceStart =>

      log.info(s"Trying to distribute tasks for targets. Timeout: [${remoteVmRequestTimeout.duration}]")
      breakpoints.foreach { breakpoint =>

        val askResult = connectionGatewayActor ? JdiVmConnectionRequest(breakpoint)
        Await.ready(askResult, remoteVmRequestTimeout.duration).value.get match {

          case Success(connectionMessage) => connectionMessage match {

            case JdiVmConnectionSuccess(resultPayload) =>
              log.info(s"Received next payload [$resultPayload] using next [$breakpoint]")
              reportActorRef ! ReportServicePayload(resultPayload)


            case JdiVmConnectionFailed(reason) =>
              log.error(s"Gateway returned failed result: [$reason]")

            case _ =>
              log.error(s"Received unknown message from: [${connectionGatewayActor.toString()}]")
          }

          case Failure(t) =>
            log.error(s"Actor failed test case execution: [${connectionGatewayActor.path}]")

        }
      }
      reportActorRef ! ReportServicePrint

    case JdiVmServiceStop =>
      log.warning("VM resources cleaning")
      context.stop(context.self)

  }

  private def connectionGatewayActorRef(testTargets: List[Target]) = {
    context.actorOf(JdiVmGatewayActor.props(testTargets), "jdi-vm-gateway")
  }

}

object JdiVmServiceActor {
  def props(reportActorRef: ActorRef)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmServiceActor(reportActorRef) with RemoteDebugToolConfig)
}
