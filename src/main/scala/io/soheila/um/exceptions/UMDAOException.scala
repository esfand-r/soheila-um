package io.soheila.um.exceptions

case class UMDAOException(message: String, cause: Throwable) extends RuntimeException(message, cause)
