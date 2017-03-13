package com.github.unknownnpc.debugtesttool.domain

sealed trait DebugTarget {

  def id: TargetId

  def address: TargetAddress

  def port: TargetPort

}

case class JvmDebugTarget(id: TargetId, address: TargetAddress, port: TargetPort) extends DebugTarget
