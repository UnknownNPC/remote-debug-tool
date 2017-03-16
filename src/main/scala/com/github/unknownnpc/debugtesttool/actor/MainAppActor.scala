package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.github.unknownnpc.debugtesttool.config.DebugTestToolConfig
import com.github.unknownnpc.debugtesttool.message.{JdiVmServiceStart, JdiVmServiceStop, MainAppActorStart, MainAppActorStop}

import scala.concurrent.ExecutionContext

class MainAppActor()(implicit actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val dispatcher: ExecutionContext = actorSystem.dispatcher

  private var jdiVmServiceActor: ActorRef = _

  override def preStart(): Unit = {
    log.info("Main app actor starts")
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

  }

  private def startServices() = {
    jdiVmServiceActor ! JdiVmServiceStart
  }

  private def stopServices() = {
    log.info("Received command to stop all services")
    jdiVmServiceActor ! JdiVmServiceStop
  }

  private def createJdiVmServiceActor() = {
    actorSystem.actorOf(Props(new JdiVmServiceActor(DebugTestToolConfig)), name = "jdi-vm-service")
  }

}
