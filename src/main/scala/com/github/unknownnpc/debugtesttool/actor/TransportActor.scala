package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging}
import com.github.unknownnpc.debugtesttool.domain.{DebugInfo, DebugTarget}

class TransportActor(debugTarget: DebugTarget) extends Actor with ActorLogging {

  override def preStart() {

  }



  override def receive = {
    case e: DebugInfo =>
    case _ =>
  }
}
