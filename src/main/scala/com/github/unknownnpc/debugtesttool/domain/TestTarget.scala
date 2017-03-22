package com.github.unknownnpc.debugtesttool.domain

trait TestTarget {
  def id: ID
  def address: Address
  def port: Port
}

case class JvmTestTarget(id: ID, address: Address, port: Port) extends TestTarget
