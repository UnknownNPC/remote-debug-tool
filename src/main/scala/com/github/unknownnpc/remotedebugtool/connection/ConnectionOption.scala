package com.github.unknownnpc.remotedebugtool.connection

import com.github.unknownnpc.remotedebugtool.domain._
import com.github.unknownnpc.remotedebugtool.exception.VmException

protected class ConnectionOption[T](option: Option[T]) {

  private val findErrorMessage = "Unable to find `%s` using next `%s`"

  def getVm = option.getOrElse(throw VmException("VM wasn't initialize"))
  def getBreakpointReq = option.getOrElse(throw VmException("Breakpoint wasn't initialize"))
  def getReferenceType(className: BreakpointClassName) = option.getOrElse(
    throw VmException(formatErrorMessage("class", className))
  )
  def getLocation(breakpointLine: BreakpointLine) = option.getOrElse(
    throw VmException(formatErrorMessage("location", breakpointLine.toString))
  )
  def getConnector = option.getOrElse(throw VmException("Unable to find `dt_socket` connection"))
  private def formatErrorMessage(unableToFind: String, using: String) = findErrorMessage.format(unableToFind, using)

}
