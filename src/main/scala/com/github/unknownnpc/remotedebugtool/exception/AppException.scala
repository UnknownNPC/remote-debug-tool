package com.github.unknownnpc.remotedebugtool.exception

trait AppException {
  self: Exception =>
}

case class VmException(message: String) extends Exception(message) with AppException
case class ConfigException(message: String) extends Exception(message) with AppException
case class ReportException(message: String) extends Exception(message) with AppException
