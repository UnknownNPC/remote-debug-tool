package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.request.{BreakpointRequest, EventRequest}
import com.sun.tools.jdi.SocketAttachingConnector

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.control.Exception._

case class JdiVmConnection(address: Address, port: Port) extends Connection {

  private val findErrorMessage = "Unable to find `%s` using next `%s`"

  private var breakpoint: BreakpointRequest = _
  private val vm: VirtualMachine = {
    val socketConnector = findSocketConnector().getOrElse(
      throw VmException("Unable to find `dt_socket` connection")
    )
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get(CONNECTOR_PORT_KEY).setValue(port.toString)
    connectorParams.get(CONNECTOR_HOSTNAME_KEY).setValue(address)
    socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }

  override def lockVm() = {
    vm.suspend()
  }

  override def unlockVm() = {
    vm.resume()
  }

  override def setBreakpoint(line: BreakpointLine, className: BreakpointClassName) = {
    val classType = vm.classesByName(className).asScala.headOption.getOrElse(throw VmException("class"))
    val location = findLocationBy(line, classType).getOrElse(throw VmException("location"))
    breakpoint = createBreakpointBy(location)
    breakpoint.enable()
  }

  override def removeBreakpoint() = {
    breakpoint.disable()
  }

  override def findValue(debugInfo: TestCase): Future[CommandExecutionResult] = {
    val thread = findThreadBy(debugInfo.breakPointThreadName).getOrElse(
      throw VmException("thread")
    )
    thread.suspend()
    val frameVars = thread.frames().asScala.flatMap(fr => safeFrameVariableSearch(fr, debugInfo.fieldName)).headOption.getOrElse(
      throw VmException("field")
    )
    try {
      val frameVars = thread.frames().asScala.flatMap(fr => safeFrameVariableSearch(fr, debugInfo.fieldName)).headOption.getOrElse(
        throw VmException("fieldName")
      )
      frameVars._2 match {
        case Some(valueExistAndVisible) =>
          val jdiValue = frameVars._1.getValue(valueExistAndVisible)
          Future.successful(
            jdiValue match {
              case sr: StringReference => sr.value()
              case ar: ArrayReference => ar.getValues.asScala.mkString
              case pv: PrimitiveValue => pv.toString
              case _ => throw VmException("Unable to handle test field type: " + jdiValue.`type`())
            }
          )
        case None => throw VmException("value")
      }
    } finally {
      thread.resume()
    }
  }

  private def safeFrameVariableSearch(f: StackFrame, t: FieldName): Option[(StackFrame, Option[LocalVariable])] = {
    failing(classOf[AbsentInformationException]) {
      Some(f, Option(f.visibleVariableByName(t)))
    }
  }

  private def failException(message: String) = Future.failed(VmException(message))

  private def exceptionMessage(value: String, property: String) = {
    findErrorMessage.format(value, property)
  }

  private def findLocationBy(breakpointLine: BreakpointLine, className: ReferenceType) = {
    className.allLineLocations.asScala.find(_.lineNumber == breakpointLine)
  }

  private def createBreakpointBy(location: Location) = {
    val erm = vm.eventRequestManager()
    val createBreakpointRequest = erm.createBreakpointRequest(location)
    createBreakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    createBreakpointRequest
  }

  private def findThreadBy(name: String) = {
    vm.allThreads().asScala.find(_.name() == name)
  }

  private def findSocketConnector() = {
    val vmm = Bootstrap.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.isInstanceOf[SocketAttachingConnector])
  }

}
