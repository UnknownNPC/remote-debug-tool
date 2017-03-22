package com.github.unknownnpc.debugtesttool.connection

import com.github.unknownnpc.debugtesttool.domain._
import com.github.unknownnpc.debugtesttool.exception.VmException

protected class ConnectionOption[T](option: Option[T]) {

  private val findErrorMessage = "Unable to find `%s` using next `%s`"

  def getVm() = option.getOrElse(throw VmException("VM wasn't initialize"))
  def getBreakpoint() = option.getOrElse(throw VmException("Breakpoint wasn't initialize"))
  def getReferenceType(className: BreakpointClassName) = option.getOrElse(throw VmException(formatErrorMessage("class", className)))
  def getLocation(breakpointLine: BreakpointLine) = option.getOrElse(throw VmException(formatErrorMessage("location", breakpointLine.toString)))
  def getConnector() = option.getOrElse(throw VmException("Unable to find `dt_socket` connection"))
  private def formatErrorMessage(unableToFind: String, using: String) = findErrorMessage.format(unableToFind, using)

}
