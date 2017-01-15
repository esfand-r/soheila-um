package io.soheila.um.daos.auths

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.um.daos.auths.InMemoryAuthTokenDAO._
import io.soheila.um.entities.AuthToken

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Give access to the InMemory [[AuthToken]] object.
 */
class InMemoryAuthTokenDAO extends AuthTokenDAO {

  override def find(id: UUID): Future[Option[AuthToken]] = Future.successful(tokens.get(id))

  override def findExpired(dateTime: LocalDateTime): Future[Seq[AuthToken]] = Future.successful {
    tokens.filter {
      case (id, token) =>
        token.expiry.isBefore(dateTime)
    }.values.toSeq
  }

  override def save(token: AuthToken): Future[AuthToken] = {
    tokens += (token.id -> token)
    Future.successful(token)
  }

  override def remove(id: UUID): Future[Unit] = {
    tokens -= id
    Future.successful(())
  }

  override def clear(): Future[Unit] = {
    tokens.clear()
    Future.successful(())
  }
}

object InMemoryAuthTokenDAO {
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
}
