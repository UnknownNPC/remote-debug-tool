package com.github.unknownnpc.remotedebugtool.domain

trait Target {
  def id: ID
  def address: Address
  def port: Port
}

case class JvmTarget(id: ID, address: Address, port: Port) extends Target
