package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._

import scala.concurrent.{ExecutionContext, Future}

class JdiVmGatewayActor(testTargets: List[TestTarget])
                       (implicit actorSystem: ActorSystem,
                        implicit val timeout: Timeout,
                        implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val jdiConnections: Map[ID, ActorRef] = testTargets.map(targetToConnection).toMap

  override def receive = {

    case message: TestCase =>
      jdiConnections.get(message.targetId) match {
        case Some(targetActorRef) => targetActorRef ? message pipeTo sender
        case _ => sender ! Future.failed(new Exception("Unable to find target server id: " + message.targetId)
        )
      }

    case _ => sender ! Future.failed(new Exception("Unable to handle message"))

  }

  private def targetToConnection(testTarget: TestTarget) = {
    testTarget.id -> actorSystem.actorOf(Props(new JdiVmConnectionActor(testTarget)),
      name = "vm-jdi-connection-id-" + testTarget.id + "-" + testTarget.address + "-" + testTarget.port)
  }

}
