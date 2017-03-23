package com.github.unknownnpc.remotedebugtool.domain

sealed trait BreakpointPayload {
  def breakpoint: Breakpoint
  def breakpointValue: BreakpointValue
}

case class JvmBreakpointPayload(breakpoint: Breakpoint,
                                breakpointValue: BreakpointValue) extends BreakpointPayload
