package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.request.EventRequest
import com.sun.tools.jdi.{SocketAttachingConnector, VirtualMachineManagerImpl}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.control.Exception._

case class VmJdiConnection(address: Address, port: Port) extends Connection {

  private val findErrorMessage = "Unable to find `%s` using next `%s`"
  private val vm: VirtualMachine = {
    val socketConnector = findSocketConnector().getOrElse(
      throw new Exception("Unable to find `dt_socket` connection")
    )
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get(CONNECTOR_PORT_KEY).setValue(port.toString)
    connectorParams.get(CONNECTOR_HOSTNAME_KEY).setValue(address)
    socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }

  override def executeCommand(debugInfo: TestCase): Future[String] = {

    val classType = vm.classesByName(debugInfo.breakPointClassName).asScala.headOption.getOrElse(
      return failException(exceptionMessage("class", debugInfo.breakPointClassName))
    )
    val location = findLocationBy(debugInfo.breakPointLine, classType).getOrElse(
      return failException(exceptionMessage("location", debugInfo.breakPointLine.toString))
    )
    val breakpointRequest = createBreakpointBy(location)
    val thread = findThreadBy(debugInfo.breakPointThreadName).getOrElse(
      return failException(exceptionMessage("thread", debugInfo.breakPointThreadName))
    )
    try {
      breakpointRequest.enable()
      thread.suspend()
      val frameVars = thread.frames().asScala.flatMap(fr => safeFrameVariableSearch(fr, debugInfo.fieldName)).headOption.getOrElse(
        return failException(exceptionMessage("variable", debugInfo.fieldName))
      )
      frameVars._2 match {
        case Some(valueExistAndVisible) =>
          val jdiValue = frameVars._1.getValue(valueExistAndVisible)
          Future.successful(
            jdiValue match {
              case bv: BooleanValue => bv.toString
              case sr: StringReference => sr.toString
              case ar: ArrayReference => ar.getValues.asScala.mkString
              case _ => throw new Exception("Unable to handle test field type: " + jdiValue.`type`())
            }
          )
        case None => failException(exceptionMessage("value", debugInfo.fieldName))
      }
    } finally {
      thread.resume()
      breakpointRequest.disable()
      failException(exceptionMessage("value", debugInfo.fieldName))
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
    val vmm = VirtualMachineManagerImpl.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.isInstanceOf[SocketAttachingConnector])
  }

}
