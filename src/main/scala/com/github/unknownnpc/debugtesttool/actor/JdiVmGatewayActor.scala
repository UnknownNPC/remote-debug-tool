package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.ExecutionContext

/*
  ExecutionContext for `pipeTo`
 */
class JdiVmGatewayActor(testTargets: List[TestTarget])(implicit timeout: Timeout,
                                                       implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val jdiConnections: Map[ID, ActorRef] = testTargets.map(targetToConnection).toMap

  override def receive = {

    case payload: JdiVmConnectionRequest =>
      val testCase = payload.testCase
      jdiConnections.get(testCase.targetId) match {

        case Some(targetActorRef) => targetActorRef ? payload pipeTo sender

        case _ => sender ! JdiVmConnectionFailed(s"Unable to find target server id: [${testCase.targetId}]")

      }

    case _ => log.warning("Unknown incoming message")

  }

  private def targetToConnection(testTarget: TestTarget) = {
    testTarget.id -> context.actorOf(JdiVmConnectionActor.props(testTarget),
      "jdi-vm-connection-id-" + testTarget.id + "-" + testTarget.address + "-" + testTarget.port)
  }

}

object JdiVmGatewayActor {
  def props(testTargets: List[TestTarget])
           (implicit timeout: Timeout, executionContext: ExecutionContext) =
    Props(new JdiVmGatewayActor(testTargets))
}
