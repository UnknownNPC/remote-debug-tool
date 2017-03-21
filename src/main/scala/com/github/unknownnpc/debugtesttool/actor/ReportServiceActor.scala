package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.unknownnpc.debugtesttool.message.{MainAppActorStop, ReportServicePayload, ReportServiceStop}
import com.github.unknownnpc.debugtesttool.report.ReportExecutor

class ReportServiceActor(mainAppActorRef: ActorRef, reportExecutor: ReportExecutor) extends Actor with ActorLogging {

  override def receive: Receive = {

    case ReportServicePayload(summaries) =>
      log.info(reportExecutor.execute(summaries).toString)
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
  def props(mainAppActorRef: ActorRef, reportExecutor: ReportExecutor) =
    Props(new ReportServiceActor(mainAppActorRef, reportExecutor))
}
