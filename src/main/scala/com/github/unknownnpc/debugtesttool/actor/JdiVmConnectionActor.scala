package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{ActorLogging, ActorRef, LoggingFSM, Props}
import com.github.unknownnpc.debugtesttool.actor.JdiVmConnectionActor._
import com.github.unknownnpc.debugtesttool.connection.{Connection, JdiVmConnection}
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message.{JdiVmConnectionFailed, JdiVmConnectionRequest, JdiVmConnectionSuccess}

import scala.language.postfixOps
import scala.util.{Failure, Success}

class JdiVmConnectionActor(testTarget: TestTarget)
  extends LoggingFSM[VmState, Data] with ActorLogging {

  private var jdiVmConnection: Connection = _

  onTransition {
    case (VMInitialized | Idle) -> VmLocked =>
      stateData match {
        case VmTask(data, fromActor) => setBreakpointTimer(VmTask(data, fromActor))
      }
    case x -> Idle => log.info("entering Idle from " + x)
  }

  private def setBreakpointTimer(vmTask: VmTask) = {
    val duration: BreakpointWaiting = vmTask.testCase.breakpointWaiting
    log.info("Set timer on: [{}]", duration)
    setTimer("waiting", (), duration, repeat = false)
  }

  startWith(VMNotInitialized, NoData)

  when(VMNotInitialized) {
    case Event(request: JdiVmConnectionRequest, _) =>
      jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)
      goto(VMInitialized) using VmTask(request.testCase, sender)
  }

  when(VMInitialized) {
    case Event(_, VmTask(data, fromActor)) =>
      jdiVmConnection.lockVm()
      jdiVmConnection.setBreakpoint(data.breakPointLine, data.breakPointClassName)
      jdiVmConnection.unlockVm()
      goto(VmLocked) using VmTask(data, fromActor)
  }

  when(VmLocked) {
    case Event(_, VmTask(data, fromActor)) =>
      val possibleResult = jdiVmConnection.findValue(data.breakPointThreadName, data.fieldName)
      possibleResult match {

        case Success(result) =>
          log.info(s"Connection received data from VM: [$result]")
          fromActor ! JdiVmConnectionSuccess(result)

        case Failure(t) =>
          val errorMessage: String = s"Failed test case execution for next case [$data]"
          log.error(errorMessage)
          fromActor ! JdiVmConnectionFailed(errorMessage)
      }
      jdiVmConnection.removeBreakpoint()
      goto(Idle) using NoData
  }

  when(Idle) {
    case Event(request: JdiVmConnectionRequest, _) =>
      log.info("Received new task")
      goto(VMInitialized) using VmTask(request.testCase, sender)

    case Event(_, NoData) =>
      log.info("Connection is waiting for new task")
      stay()

  }

  initialize()
}

object JdiVmConnectionActor {

  def props(testTarget: TestTarget) = Props(new JdiVmConnectionActor(testTarget))

  trait Data
  case object NoData extends Data
  case class VmTask(testCase: TestCase, fromActor: ActorRef) extends Data

  trait VmState
  case object VMNotInitialized extends VmState
  case object VMInitialized extends VmState
  case object VmLocked extends VmState
  case object Idle extends VmState

}
