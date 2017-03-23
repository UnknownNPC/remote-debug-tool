package com.github.unknownnpc.remotedebugtool.domain

trait Breakpoint {
  def targetId: ID
  def line: BreakpointLine
  def className: BreakpointClassName
  def eventTriggerTimeout: BreakpointEventTriggerTimeout
  def fieldName: FieldName
}

case class JvmBreakpoint(targetId: ID,
                         line: BreakpointLine,
                         className: BreakpointClassName,
                         eventTriggerTimeout: BreakpointEventTriggerTimeout,
                         fieldName: FieldName) extends Breakpoint
