package com.github.unknownnpc.debugtesttool.exception

trait AppException {
  self: Exception =>
}

case class VmException(message: String) extends Exception(message) with AppException
case class ConfigException(message: String) extends Exception(message) with AppException
