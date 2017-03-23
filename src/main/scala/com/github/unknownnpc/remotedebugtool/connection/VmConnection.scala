package com.github.unknownnpc.remotedebugtool.connection

import com.github.unknownnpc.remotedebugtool.domain._

trait VmConnection {

  def lockVm()

  def unlockVm()

  def connect()

  def disconnect()

  def enableBreakpoint(line: BreakpointLine, className: BreakpointClassName)

  def disableBreakpoint()

  def findValue(fieldName: FieldName, searchTimeout: BreakpointEventTriggerTimeout): Option[BreakpointValue]

}
