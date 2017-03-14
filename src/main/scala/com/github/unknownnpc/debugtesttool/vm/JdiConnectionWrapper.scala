package com.github.unknownnpc.debugtesttool.vm


import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.request.EventRequest
import com.sun.tools.jdi.{SocketAttachingConnector, VirtualMachineManagerImpl}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.control.Exception._

case class JdiConnectionWrapper(address: TargetAddress, port: TargetPort) extends Transport {

  private val findErrorMessage = "Unable to find `%s` using next `%s`"
  private val vm: VirtualMachine = {
    val socketConnector = findSocketConnector().getOrElse(
      throw new Exception("Unable to find `dt_socket` connection")
    )
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get("port").setValue(port.toString)
    connectorParams.get("hostname").setValue(address)
    socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }

  override def executeCommand(debugInfo: DebugInfo): Future[String] = {

    val classType = vm.classesByName(debugInfo.breakPointClassName).asScala.headOption.getOrElse(
      return buildFailResult(buildExceptionMessage("class", debugInfo.breakPointClassName))
    )
    val location = findLocationBy(debugInfo.breakPointLine, classType).getOrElse(
      return buildFailResult(buildExceptionMessage("location", debugInfo.breakPointLine.toString))
    )
    val breakpointRequest = createBreakpointBy(location)
    val thread = findThreadBy(debugInfo.breakPointThreadName).getOrElse(
      return buildFailResult(buildExceptionMessage("thread", debugInfo.breakPointThreadName))
    )
    try {
      breakpointRequest.enable()
      thread.suspend()
      val frameVars = thread.frames().asScala.map(fr => safeFrameVariableSearch(fr, debugInfo.testFieldName)).head.getOrElse(
        return buildFailResult(buildExceptionMessage("variable", debugInfo.testFieldName))
      )
      val value = frameVars._1.getValue(frameVars._2)
      Future.successful(value.toString)
    } finally {
      thread.resume()
      breakpointRequest.disable()
      buildFailResult(buildExceptionMessage("value", debugInfo.testFieldName))
    }
  }

  private def safeFrameVariableSearch(f: StackFrame, t: TestFieldName): Option[(StackFrame, LocalVariable)] = {
    failing(classOf[AbsentInformationException]) opt Tuple2(f, f.visibleVariableByName(t))
  }

  private def buildFailResult(message: String) = Future.failed(VmException(message))

  private def buildExceptionMessage(value: String, property: String) = {
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
