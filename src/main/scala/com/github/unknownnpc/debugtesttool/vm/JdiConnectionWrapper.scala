package com.github.unknownnpc.debugtesttool.vm


import com.github.unknownnpc.debugtesttool.domain._
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.request.EventRequest
import com.sun.tools.jdi.{SocketAttachingConnector, VirtualMachineManagerImpl}

import scala.collection.JavaConverters._
import scala.concurrent.Future

case class JdiConnectionWrapper(address: TargetAddress, port: TargetPort) extends Transport {

  private val vm: VirtualMachine = {
    val socketConnector = findSocketConnector().getOrElse(
      throw new Exception("Unable to find dt_socket connection")
    )
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get("port").setValue(port.toString)
    connectorParams.get("hostname").setValue(address)
    socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }

  override def executeCommand(debugInfo: DebugInfo): Future[String] = {

    val className = vm.classesByName(debugInfo.breakPointClassName).asScala.head
    val location = findLocationBy(debugInfo.breakPointLine, className).getOrElse(
      throw new Exception("Unable to find `location` with next line: " + debugInfo.breakPointLine)
    )
    val breakpointRequest = createBreakpointBy(location)
    val thread = findThreadBy(debugInfo.breakPointThreadName).getOrElse(
      throw new Exception("Unable find `thread` for name: " + debugInfo.breakPointThreadName)
    )
    try {
      breakpointRequest.enable()
      thread.suspend()
      val frameVars = thread.frames().asScala.map(f => (f, f.visibleVariableByName(debugInfo.testFieldName))).head
      val value = frameVars._1.getValue(frameVars._2)
      Future.successful(
        value.toString
      )
    } finally {
      thread.resume()
      breakpointRequest.disable()
      Future.failed(
        new Exception("fail")
      )
    }
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

  private def findFrameByClassName(threadRef: ThreadReference, breakPointClassName: BreakpointClassName) = {
    threadRef.frames().asScala.find(_.location().method().declaringType().name() == breakPointClassName)
  }

  private def findSocketConnector() = {
    val vmm = VirtualMachineManagerImpl.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.isInstanceOf[SocketAttachingConnector])
  }
}
