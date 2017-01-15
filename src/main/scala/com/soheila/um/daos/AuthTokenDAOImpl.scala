package com.soheila.um.daos

import java.util.UUID

import AuthTokenDAOImpl._
import org.joda.time.DateTime
import com.soheila.um.entities.AuthToken

import scala.collection.mutable
import scala.concurrent.Future

// Todo: Use mongo for persistence but we might not need this class at all.
class AuthTokenDAOImpl extends AuthTokenDAO {

  def find(id: UUID) = Future.successful(tokens.get(id))

  def findExpired(dateTime: DateTime) = Future.successful {
    tokens.filter {
      case (id, token) =>
        token.expiry.isBefore(dateTime)
    }.values.toSeq
  }

  def save(token: AuthToken) = {
    tokens += (token.id -> token)
    Future.successful(token)
  }

  def remove(id: UUID) = {
    tokens -= id
    Future.successful(())
  }
}

object AuthTokenDAOImpl {
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
}
