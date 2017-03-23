package com.github.unknownnpc.remotedebugtool

import scala.concurrent.duration.FiniteDuration

package object domain {

  type ID = Long
  type Address = String
  type Port = Int
  type BreakpointLine = Int
  type BreakpointThreadName = String
  type BreakpointClassName = String
  type FieldName = String
  type BreakpointEventTriggerTimeout = FiniteDuration

  type BreakpointValue = String
  type CommandFailReason = String

}
