package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import com.sun.jdi.event.{BreakpointEvent, EventSet}
import com.sun.jdi.request.{BreakpointRequest, EventRequest}
import com.sun.tools.jdi.SocketAttachingConnector
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

case class JdiVmConnection(address: Address, port: Port) extends VmConnection {


  private val log = LoggerFactory.getLogger(this.getClass)
  private val findErrorMessage = "Unable to find `%s` using next `%s`"
  private val vm: VirtualMachine = {
    val socketConnector = findSocketConnector().getOrElse(
      throw VmException("Unable to find `dt_socket` connection")
    )
    val connectorParams = socketConnector.defaultArguments()
    connectorParams.get(CONNECTOR_PORT_KEY).setValue(port.toString)
    connectorParams.get(CONNECTOR_HOSTNAME_KEY).setValue(address)
    socketConnector.asInstanceOf[AttachingConnector].attach(connectorParams)
  }
  private var breakpoint: BreakpointRequest = _

  override def lockVm() = {
    vm.suspend()
  }

  override def unlockVm() = {
    vm.resume()
  }

  override def setBreakpoint(line: BreakpointLine, className: BreakpointClassName) = {
    val classRef = vm.classesByName(className).asScala.headOption.getOrElse(throw VmException("class"))
    val location = findLocationBy(line, classRef).getOrElse(throw VmException("location"))
    breakpoint = createBreakpointBy(location)
    breakpoint.enable()
  }

  private def findLocationBy(breakpointLine: BreakpointLine, classRef: ReferenceType) = {
    classRef.allLineLocations.asScala.find(_.lineNumber == breakpointLine)
  }

  private def createBreakpointBy(location: Location) = {
    val erm = vm.eventRequestManager()
    val createBreakpointRequest = erm.createBreakpointRequest(location)
    createBreakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    createBreakpointRequest
  }

  override def removeBreakpoint() = {
    breakpoint.disable()
  }

  override def findValue(fieldName: FieldName, searchTimeout: BreakpointEventTriggerTimeout) = {

    val evtQueue = vm.eventQueue()

    log.debug(s"Variable search process started. Timeout: [${searchTimeout.toSeconds}] seconds")
    val optionalTriggeredEventSet = Option(evtQueue.remove(searchTimeout.toMillis))

    val searchResult = optionalTriggeredEventSet match {
      case Some(triggeredEventSet) =>

        val values = searchInEventSet(fieldName, triggeredEventSet)
        triggeredEventSet.resume()
        values.head

      case None =>
        log.debug(s"Breakpoint event wasn't triggered. Nothing was found")
        None
    }


    log.debug(s"Found next results: \n ${searchResult.mkString("\n")}")
    searchResult
  }

  private def searchInEventSet(fieldName: FieldName,
                               triggeredEventSet: EventSet): List[Option[CommandExecutionResult]] = {

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
                case _ => log.error(formatErrorMessage("value in frames", fieldName)); None
              }

            case None => log.error(formatErrorMessage("value in frames", fieldName)); None
          }
      }
    }.toList
  }

  private def formatErrorMessage(unableToFind: String, using: String) = {
    findErrorMessage.format(unableToFind, using)
  }

  private def findThreadBy(name: String) = {
    vm.allThreads().asScala.find(_.name() == name)
  }

  private def findSocketConnector() = {
    val vmm = Bootstrap.virtualMachineManager()
    vmm.allConnectors().asScala.find(_.isInstanceOf[SocketAttachingConnector])
  }

}
