package com.github.unknownnpc.debugtesttool.domain

sealed trait DebugTarget {

  def id: Id
  def address: Address
  def port: Port

}

case class JvmDebugTarget(id: Id, address: Address, port: Port) extends DebugTarget
