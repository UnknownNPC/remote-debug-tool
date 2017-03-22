package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.unknownnpc.debugtesttool.message.{MainAppActorStop, ReportServicePayload, ReportServiceStop}
import com.github.unknownnpc.debugtesttool.report.ReportFormatter

class ReportServiceActor(mainAppActorRef: ActorRef, reportFormatter: ReportFormatter) extends Actor with ActorLogging {

  override def receive: Receive = {

    case ReportServicePayload(reports) =>
      log.info(reportFormatter.format(reports))
      //stop app
      mainAppActorRef ! MainAppActorStop

    case ReportServiceStop =>
      context.stop(self)
      log.warning("Stop command received")

    case _ =>
      log.warning("Unknown incoming message")

  }
}

object ReportServiceActor {
  def props(mainAppActorRef: ActorRef, reportFormatter: ReportFormatter) =
    Props(new ReportServiceActor(mainAppActorRef, reportFormatter))
}
