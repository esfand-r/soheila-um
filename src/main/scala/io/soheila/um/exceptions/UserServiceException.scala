package io.soheila.um.exceptions

case class UserServiceException(message: String, cause: Throwable) extends RuntimeException(message, cause)
