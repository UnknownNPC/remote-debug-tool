package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging}
import com.github.unknownnpc.debugtesttool.domain.{TestInfo, TestTarget}

class TransportActor(debugTarget: TestTarget) extends Actor with ActorLogging {

  override def preStart() {

  }



  override def receive = {
    case e: TestInfo =>
    case _ =>
  }
}
