package io.soheila.um.exceptions

case class ActivityServiceException(message: String, cause: Throwable) extends RuntimeException(message, cause)
