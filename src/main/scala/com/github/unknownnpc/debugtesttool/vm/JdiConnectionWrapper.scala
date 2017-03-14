package com.github.unknownnpc.debugtesttool.vm


import com.github.unknownnpc.debugtesttool.domain._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.event.{ClassPrepareEvent, EventIterator}
import com.sun.jdi.request.BreakpointRequest
import com.sun.jdi._
import com.sun.tools.jdi.VirtualMachineManagerImpl

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future

case class JdiConnectionWrapper(address: TargetAddress, port: TargetPort) extends Transport {

  private def createVm(): VirtualMachine = {
    val connector = findConnectorBy("dt_socket").getOrElse(throw new Exception("Unable to find dt_socket connection"))
    val connectorParams = connector.defaultArguments()
    connectorParams.get("port").setValue(port.toString)
    connectorParams.get("hostname").setValue(address)
    connector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }

  private def dropVm(vm: VirtualMachine): Unit = {
    //check it
    vm.dispose()
  }

  private def findConnectorBy(name: String) = {
    val vmm = VirtualMachineManagerImpl.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.transport().name() == name)
  }

  private def findThreadBy(vm: VirtualMachine, name: String) = {
    vm.allThreads().asScala.find(_.name() == name)
  }

  private def createBreakpoint(vm: VirtualMachine, threadFrame: StackFrame, breakpointLine: BreakpointLine) = {
    val evReqMan = vm.eventRequestManager()
    evReqMan.createBreakpointRequest(threadFrame.location())
  }

  private def getValueBy(testFieldName: TestFieldName, threadRef: ThreadReference) = {
    //head is'nt good. refactor after test
    val localVar = threadRef.frames().asScala.map(_.visibleVariableByName(testFieldName)).head
    localVar.signature()
  }

  private def findFrameByClassName(threadRef: ThreadReference, breakPointClassName: BreakpointClassName) = {
    threadRef.threadGroup()
    threadRef.frames().asScala.find(_.location().method().declaringType().name() == breakPointClassName)
  }

  override def executeCommand(debugInfo: DebugInfo): Future[TransportExecutionResult] = {
    val vm: VirtualMachine = createVm()
    ///////////////////////////////////
    ///run
    vm.resume()
    //////////////////
    val eventRequestManager = vm.eventRequestManager()
    ///////////////////
    //// This event you need to handle for proper shut Debugger
    val vmdeath =  eventRequestManager.createVMDeathRequest()
    vmdeath.enable()
    /////
    val classRef = vm.classesByName(debugInfo.breakPointClassName).get(0)
    val locations = classRef.allLineLocations().asScala

    // After registration event is still necessary to permit this type of event
    //vmDeathRequest.enable ();
    //vmDeathRequest return;

    ///////////////////////////////////

/*    val meth = classRef.methodsByName("run").get(0)
    classRef.allLineLocations()
    val brF1 = vm.eventRequestManager().createBreakpointRequest(meth.location())*/


    /*vm.suspend()
    //////////////////
    val reqMan = vm.eventRequestManager()
    val r = reqMan.createClassPrepareRequest()
    r.addClassFilter(debugInfo.breakPointClassName)
    r.enable()

    val eventQueue = vm.eventQueue()
    val eventSet = eventQueue.remove()
    val eventIterator = eventSet.eventIterator()
    if (eventIterator.hasNext()) {
      val event = eventIterator.next()
      val b: Boolean = event.isInstanceOf[ClassPrepareEvent]
      if(b) {
        val evt = event.asInstanceOf[ClassPrepareEvent]
        val classType = evt.referenceType()
        val locations = classType.locationsOfLine(55)
        val locationForBreakpoint = locations.get(0)

        vm.resume()
      }
    }*/


    ////////////////////////

   /* val threadRef = findThreadBy(vm, debugInfo.breakPointThreadName).getOrElse(
      throw new Exception("Unable to find thread: " + debugInfo.breakPointThreadName)
    )
    threadRef.suspend()
    val threadFrame = findFrameByClassName(threadRef, debugInfo.breakPointClassName).getOrElse(
      throw new Exception("Unable to find frame: " + debugInfo.breakPointClassName)
    )

    val breakpoint: BreakpointRequest = createBreakpoint(vm, threadFrame, debugInfo.breakPointLine)

    breakpoint.enable()
    val value: TargetAddress = getValueBy(debugInfo.testFieldName, threadRef)
    threadRef.resume()
    breakpoint.disable()*/

    Future.successful("1")
  }
}
