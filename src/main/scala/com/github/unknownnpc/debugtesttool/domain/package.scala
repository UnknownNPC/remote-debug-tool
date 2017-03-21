package com.github.unknownnpc.debugtesttool

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

  type TestCaseValue = String
  type CommandFailReason = String

}
