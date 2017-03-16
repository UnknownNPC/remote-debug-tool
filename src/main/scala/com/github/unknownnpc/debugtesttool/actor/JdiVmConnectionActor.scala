package com.github.unknownnpc.debugtesttool.actor

import akka.actor.{ActorLogging, LoggingFSM, Props}
import com.github.unknownnpc.debugtesttool.actor.JdiVmConnectionActor._
import com.github.unknownnpc.debugtesttool.connection.{Connection, JdiVmConnection}
import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.message.JdiVmConnectionRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class JdiVmConnectionActor(testTarget: TestTarget) extends LoggingFSM[VmState, Data] with ActorLogging {

  private var jdiVmConnection: Connection = _

  startWith(VMNotInitialized, NoData)

  when(VMNotInitialized) {
    case Event(request: JdiVmConnectionRequest, _) =>
      jdiVmConnection = JdiVmConnection(testTarget.address, testTarget.port)
      goto(VMInitialized) using VmTask(request.testCase)
  }

  when(VMInitialized) {
    case Event(_, VmTask(data)) =>
      jdiVmConnection.lockVm()
      jdiVmConnection.setBreakpoint(data.breakPointLine)
      jdiVmConnection.unlockVm()
      goto(VmLocked) using VmTask(data)
  }

  //idle time from test case
  when(VmLocked, stateTimeout = 20 seconds) {
    case Event(StateTimeout, VmTask(data)) =>
      sender() ! jdiVmConnection.findValue(data)
      jdiVmConnection.removeBreakpoint()
      goto(Idle) using NoData
  }

  when(Idle) {
    case Event(request: JdiVmConnectionRequest, _) =>
      goto(VMInitialized) using VmTask(request.testCase)
  }

  initialize()
}

object JdiVmConnectionActor {
  def props(testTarget: TestTarget)(implicit executionContext: ExecutionContext) =
    Props(new JdiVmConnectionActor(testTarget))

  trait Data
  case object NoData extends Data
  case class VmTask(testCase: TestCase) extends Data

  trait VmState
  case object VMNotInitialized extends VmState
  case object VMInitialized extends VmState
  case object VmLocked extends VmState
  case object Idle extends VmState

}
