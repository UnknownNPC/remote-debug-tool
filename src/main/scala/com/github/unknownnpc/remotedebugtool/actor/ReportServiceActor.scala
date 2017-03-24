package com.github.unknownnpc.remotedebugtool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.unknownnpc.remotedebugtool.config.{AppConfig, RemoteDebugToolConfig}
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.exception.ReportException
import com.github.unknownnpc.remotedebugtool.message.{MainAppActorStop, ReportServicePayload, ReportServicePrint}

import scala.collection.mutable.ListBuffer

class ReportServiceActor(mainAppActorRef: ActorRef) extends Actor with ActorLogging {

  self: AppConfig =>

  private val values = ListBuffer.empty[ReportRow]

  override def receive = {

    case ReportServicePayload(payload) =>

      log.debug(s"Print service received incoming payload: [$payload]")
      values += reportRowFrom(payload)

    case ReportServicePrint =>

      log.debug(s"Received print command")
      log.info(systemConfig.reportFormatter.format(values.toList))
      mainAppActorRef ! MainAppActorStop

  }


  private def reportRowFrom(payload: BreakpointPayload) = {
    val testTarget = findServerById(payload.breakpoint.targetId)
    JvmReportRow(testTarget.id,
      testTarget.address,
      testTarget.port,
      payload.breakpoint.line,
      payload.breakpoint.className,
      payload.breakpointValue
    )
  }

  private def findServerById(id: ID) = {
    servers.find(_.id == id).getOrElse(
      throw ReportException("Unable to match payload to server instance")
    )
  }

}

object ReportServiceActor {
  def props(mainAppActorRef: ActorRef) =
    Props(new ReportServiceActor(mainAppActorRef) with RemoteDebugToolConfig)
}
