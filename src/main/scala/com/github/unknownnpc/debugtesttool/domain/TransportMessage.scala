package com.github.unknownnpc.debugtesttool.domain

trait TransportMessage

case class Success(result: String) extends TransportMessage
object Failure extends TransportMessage