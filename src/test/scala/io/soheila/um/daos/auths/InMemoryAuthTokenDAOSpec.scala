package io.soheila.um.daos.auths

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.um.entities.AuthToken
import org.specs2._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }

class InMemoryAuthTokenDAOSpec(implicit ec: ExecutionContext) extends Specification {
  def is = s2"""
    This is a specification for the InMemory AuthToken DAO
    The 'InMemoryAuthTokenDAO' should
      save a new token                                  $e1
      find a saved Token by it's uuid                   $e2
      remove a saved Token by it's uuid                 $e3
      find expired tokens                               $e4
                                                        """

  def e1 = {
    val authTokenDAO = new InMemoryAuthTokenDAO

    val uuid = UUID.randomUUID()
    val userUuid = UUID.randomUUID()
    val date = LocalDateTime.now()

    val authToken = AuthToken(uuid, userUuid, date)
    val savedToken = Await.result(authTokenDAO.save(authToken), 5 seconds)

    savedToken.expiry must beEqualTo(date)
    savedToken.userID must beEqualTo(userUuid)
    savedToken.id must beEqualTo(uuid)
  }

  def e2 = {
    val authTokenDAO = new InMemoryAuthTokenDAO

    val uuid = UUID.randomUUID()
    val userUuid = UUID.randomUUID()
    val date = LocalDateTime.now()

    val authToken = AuthToken(uuid, userUuid, date)
    Await.result(authTokenDAO.save(authToken), 5 seconds)

    val foundToken = Await.result(authTokenDAO.find(uuid), 5 seconds).get
    foundToken.expiry must beEqualTo(date)
    foundToken.userID must beEqualTo(userUuid)
    foundToken.id must beEqualTo(uuid)
  }

  def e3 = {
    val authTokenDAO = new InMemoryAuthTokenDAO

    val uuid = UUID.randomUUID()
    val userUuid = UUID.randomUUID()
    val date = LocalDateTime.now()

    val authToken = AuthToken(uuid, userUuid, date)
    Await.result(authTokenDAO.save(authToken), 5 seconds)

    val foundToken = Await.result(authTokenDAO.find(uuid), 5 seconds)
    foundToken must not beEmpty

    Await.result(authTokenDAO.remove(uuid), 5 seconds)

    val foundTokenAfterRemove = Await.result(authTokenDAO.find(uuid), 5 seconds)
    foundTokenAfterRemove must beEmpty
  }

  def e4 = {
    val authTokenDAO = new InMemoryAuthTokenDAO
    Await.result(authTokenDAO.clear(), 5 seconds)

    val uuid = UUID.randomUUID()
    val userUuid = UUID.randomUUID()
    val date = LocalDateTime.now().plusDays(1)

    val uuid2 = UUID.randomUUID()
    val userUuid2 = UUID.randomUUID()
    val date2 = LocalDateTime.now().minusDays(1)

    val uuid3 = UUID.randomUUID()
    val userUuid3 = UUID.randomUUID()
    val date3 = LocalDateTime.now().minusDays(1)

    val authToken = AuthToken(uuid, userUuid, date)
    val authToken2 = AuthToken(uuid2, userUuid2, date2)
    val authToken3 = AuthToken(uuid3, userUuid3, date3)

    Await.result(authTokenDAO.save(authToken), 5 seconds)
    Await.result(authTokenDAO.save(authToken2), 5 seconds)
    Await.result(authTokenDAO.save(authToken3), 5 seconds)

    val foundTokens = Await.result(authTokenDAO.findExpired(LocalDateTime.now()), 5 seconds)

    foundTokens.size must beEqualTo(2)
  }

}