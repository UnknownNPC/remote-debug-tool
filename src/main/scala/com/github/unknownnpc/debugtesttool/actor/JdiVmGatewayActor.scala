package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.ExecutionContext

class JdiVmGatewayActor(testTargets: List[TestTarget])
                       (implicit actorSystem: ActorSystem,
                        implicit val timeout: Timeout,
                        implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val jdiConnections: Map[ID, ActorRef] = testTargets.map(targetToConnection).toMap

  override def receive = {

    case payload: JdiVmConnectionRequest =>
      val testCase = payload.testCase
      jdiConnections.get(testCase.targetId) match {

        case Some(targetActorRef) => targetActorRef ? testCase pipeTo sender

        case _ => sender ! JdiVmConnectionFailed(s"Unable to find target server id: [${testCase.targetId}]")

      }

    case _ => sender ! JdiVmConnectionFailed("Unknown incoming message")

  }

  private def targetToConnection(testTarget: TestTarget) = {
    testTarget.id -> actorSystem.actorOf(Props(new JdiVmConnectionActor(testTarget)),
      name = "jdi-vm-connection-id-" + testTarget.id + "-" + testTarget.address + "-" + testTarget.port)
  }

}
