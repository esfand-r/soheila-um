package com.soheila.um.exceptions

case class DAOException(message: String, cause: Throwable) extends RuntimeException(message, cause)
