package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JdiVmGatewayActor(testTargets: List[TestTarget])
                       (implicit actorSystem: ActorSystem,
                        implicit val timeout: Timeout,
                        implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val jdiConnections: Map[ID, ActorRef] = testTargets.map(targetToConnection).toMap

  override def receive = {

    case payload: ConnectionGatewayPayload =>
      val testCase = payload.testCase
      jdiConnections.get(testCase.targetId) match {
        case Some(targetActorRef) =>
          (targetActorRef ? testCase).onComplete {
            case Success(futureResult) => futureResult match {
              case JdiVmConnectionSuccess(resultPayload) => sender ! ConnectionGatewaySuccess(resultPayload)
              case JdiVmConnectionFailed(reason) => sender ! ConnectionGatewayFailed(reason)
              case _ => log.error(s"Received unknown message from: [${targetActorRef.toString()}]")
            }
            case Failure(t) =>
              log.error(s"Connection actor failed test case execution: [${targetActorRef.toString()}]")
              sender ! JdiVmConnectionFailed(t.getMessage)
          }
        case _ => sender ! ConnectionGatewayFailed(s"Unable to find target server id: [${testCase.targetId}]")
      }
    case _ => sender ! ConnectionGatewayFailed("Unknown incoming message")

  }

  private def targetToConnection(testTarget: TestTarget) = {
    testTarget.id -> actorSystem.actorOf(Props(new JdiVmConnectionActor(testTarget)),
      name = "vm-jdi-connection-id-" + testTarget.id + "-" + testTarget.address + "-" + testTarget.port)
  }

}
