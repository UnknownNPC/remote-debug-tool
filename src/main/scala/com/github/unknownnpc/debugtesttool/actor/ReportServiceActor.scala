package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.unknownnpc.debugtesttool.config.{AppConfig, DebugTestToolConfig}
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.ReportException
import com.github.unknownnpc.debugtesttool.message.{MainAppActorStop, ReportServicePayload, ReportServicePrint}

import scala.collection.mutable.ListBuffer

class ReportServiceActor(mainAppActorRef: ActorRef) extends Actor with ActorLogging {

  self: AppConfig =>

  val values = ListBuffer.empty[ReportRow]

  override def receive = {

    case ReportServicePayload(payload) =>
      log.debug(s"Print service received incoming payload: [$payload]")
      values += payloadToRow(payload)

    case ReportServicePrint =>
      log.debug(s"Received print command")
      log.info(systemConfig.reportFormatter.format(values.toList))
      mainAppActorRef ! MainAppActorStop

  }


  private def payloadToRow(payload: ExecutionPayload) = {
    val testTarget = findTargetById(payload.testCase.targetId)
    JvmReportRow(testTarget.id,
      testTarget.address,
      testTarget.port,
      payload.testCase.breakPointLine,
      payload.testCase.breakPointClassName,
      payload.testCaseValue
    )
  }

  private def findTargetById(id: ID) = {
    testTargets.find(_.id == id).getOrElse(
      throw ReportException("Unable to match payload to target instance")
    )
  }

}

object ReportServiceActor {
  def props(mainAppActorRef: ActorRef) =
    Props(new ReportServiceActor(mainAppActorRef) with DebugTestToolConfig)
}
