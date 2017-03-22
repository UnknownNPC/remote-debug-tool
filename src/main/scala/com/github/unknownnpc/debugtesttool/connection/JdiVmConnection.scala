package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.event.{BreakpointEvent, EventSet}
import com.sun.jdi.request.{BreakpointRequest, EventRequest}
import com.sun.tools.jdi.SocketAttachingConnector
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.language.implicitConversions

case class JdiVmConnection(address: Address, port: Port) extends VmConnection {

  private val log = LoggerFactory.getLogger(this.getClass)

  private var vm: Option[VirtualMachine] = None
  private var breakpoint: Option[BreakpointRequest] = None

  implicit def tuneOption[T](option: Option[T]): ConnectionOption[T] = new ConnectionOption(option)

  override def lockVm() {
    vm.getVm.suspend()
  }

  override def unlockVm() {
    vm.getVm.resume()
  }

  override def connect() {
    val socketConnector = findSocketConnector().getConnector
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get(CONNECTOR_PORT_KEY).setValue(port.toString)
    connectorParams.get(CONNECTOR_HOSTNAME_KEY).setValue(address)
    vm = Option(socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams))
  }

  override def disconnect() {
    vm.getVm.dispose()
  }

  override def enableBreakpoint(line: BreakpointLine, className: BreakpointClassName) {
    val referenceType = vm.getVm.classesByName(className).asScala.headOption.getReferenceType(className)
    val location = findLocationBy(line, referenceType).getLocation(line)
    breakpoint = Option(createBreakpointBy(location))
    breakpoint.getBreakpoint.enable()
  }

  override def disableBreakpoint() {
    breakpoint.getBreakpoint.disable()
  }

  private def findLocationBy(breakpointLine: BreakpointLine, classRef: ReferenceType) = {
    classRef.allLineLocations.asScala.find(_.lineNumber == breakpointLine)
  }

  private def createBreakpointBy(location: Location) = {
    val erm = vm.getVm.eventRequestManager()
    val createBreakpointRequest = erm.createBreakpointRequest(location)
    createBreakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    createBreakpointRequest
  }

  override def findValue(fieldName: FieldName, searchTimeout: BreakpointEventTriggerTimeout) = {

    val evtQueue = vm.getVm.eventQueue()

    log.debug(s"Variable search process started. Timeout: [${searchTimeout.toSeconds}] seconds")
    val optionalTriggeredEventSet = Option(evtQueue.remove(searchTimeout.toMillis))

    val headSearchValue = optionalTriggeredEventSet match {
      case Some(triggeredEventSet) =>

        val values = searchInEventSet(fieldName, triggeredEventSet)
        log.trace(s"Found next results: \n ${values.mkString("\n")}")
        triggeredEventSet.resume()
        values.head

      case None =>
        log.error(s"Breakpoint event wasn't triggered. Nothing was found")
        None
    }
    headSearchValue
  }

  private def searchInEventSet(fieldName: FieldName,
                               triggeredEventSet: EventSet): List[Option[TestCaseValue]] = {
    triggeredEventSet.eventIterator().asScala.map { event =>
      event.request() match {
        case breakpointRequest: BreakpointRequest =>
          val breakpointEvent = event.asInstanceOf[BreakpointEvent]
          val stackFrame = breakpointEvent.thread().frames().asScala.head
          val localVariable = stackFrame.visibleVariables().asScala.find(_.name() == fieldName)

          localVariable match {
            case Some(variable) =>

              val jdiValue = stackFrame.getValue(variable)
              jdiValue match {
                case sr: StringReference => Option(sr.value())
                case ar: ArrayReference => Option(ar.getValues.asScala.mkString)
                case pv: PrimitiveValue => Option(pv.toString)
                case or: ObjectReference => Option(or.toString)
                case _ => log.error("Unable to detect `value` type"); None
              }

            case None => log.error("Unable to find value in frames"); None
          }
      }
    }.toList
  }


  private def findThreadBy(name: String) = {
    vm.getVm.allThreads().asScala.find(_.name() == name)
  }

  private def findSocketConnector() = {
    val vmm = Bootstrap.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.isInstanceOf[SocketAttachingConnector])
  }

}
