package com.github.unknownnpc.debugtesttool.domain

sealed trait TestTarget {

  def id: ID

  def address: Address

  def port: Port

}

case class JvmTestTarget(id: ID, address: Address, port: Port) extends TestTarget
