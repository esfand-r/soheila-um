package com.soheila.um.exceptions

case class AuthenticationException(message: String, cause: Throwable) extends RuntimeException(message, cause)
