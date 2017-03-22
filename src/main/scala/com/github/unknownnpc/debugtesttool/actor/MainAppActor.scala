package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.ExecutionContext

class MainAppActor()(implicit actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val dispatcher: ExecutionContext = actorSystem.dispatcher

  private var jdiVmServiceActor: ActorRef = _
  private var reportServiceActor: ActorRef = _

  override def preStart(): Unit = {
    log.info("Main app actor starts")
    reportServiceActor = createReportServiceActor()
    jdiVmServiceActor = createJdiVmServiceActor()
  }

  override def postStop() {
    log.info("Main app actor stops")
  }

  override def receive: Receive = {

    case MainAppActorStart =>
      startServices()

    case MainAppActorStop =>
      stopServices()
      context.stop(self)
      context.system.terminate()

  }

  private def startServices() = {
    jdiVmServiceActor ! JdiVmServiceStart
  }

  private def stopServices() = {
    log.info("Received command to stop all services")
    jdiVmServiceActor ! JdiVmServiceStop
    reportServiceActor ! ReportServiceStop
  }

  private def createJdiVmServiceActor() = {
    context.actorOf(JdiVmServiceActor.props(reportServiceActor), "jdi-vm-service")
  }

  private def createReportServiceActor() = {
    context.actorOf(ReportServiceActor.props(self), "report-service")
  }

}
