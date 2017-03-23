package com.github.unknownnpc.remotedebugtool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.remotedebugtool.connection.JdiVmConnection
import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.message._

import scala.concurrent.ExecutionContext


class JdiVmGatewayActor(testTargets: List[Target])(implicit askTimeout: Timeout,
                                                   implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  private val jdiConnections: Map[ID, ActorRef] = testTargets.map(targetToConnection).toMap

  override def receive = {

    case message: JdiVmConnectionRequest =>
      jdiConnections.get(message.breakpoint.targetId) match {

        case Some(targetActorRef) => targetActorRef ? message pipeTo sender

        case _ => sender ! JdiVmConnectionFailed(s"Unable to find target server id: [${message.breakpoint.targetId}]")

      }

    case _ => log.warning("Unknown incoming message")

  }

  private def targetToConnection(testTarget: Target) = {
    testTarget.id -> context.actorOf(JdiVmConnectionActor.props(JdiVmConnection(testTarget.address, testTarget.port)),
      "jdi-vm-connection-id-" + testTarget.id + "-" + testTarget.address + "-" + testTarget.port)
  }

}

object JdiVmGatewayActor {
  def props(targets: List[Target])
           (implicit timeout: Timeout, executionContext: ExecutionContext) =
    Props(new JdiVmGatewayActor(targets))
}
