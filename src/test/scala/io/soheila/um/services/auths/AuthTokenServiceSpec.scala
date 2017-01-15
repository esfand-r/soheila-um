package io.soheila.um.services.auths

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.um.daos.auths.AuthTokenDAO
import io.soheila.um.entities.AuthToken
import io.soheila.um.utils.UMClock
import org.specs2.Specification
import org.specs2.mock.Mockito

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class AuthTokenServiceSpec extends Specification with Mockito {
  def is = sequential ^ s2"""
    'create' method create an AuthToken and specified expiry and user's UUID     $e1
    'validate' should find the token by UUID     $e2
    'clean' should remove all the expired tokens    $e3
                                                        """

  def e1 = {
    val authTokenDAOMock: AuthTokenDAO = mock[AuthTokenDAO]
    val umClockMock: UMClock = mock[UMClock]

    val authTokenService: AuthTokenServiceImpl = new AuthTokenServiceImpl(authTokenDAOMock, umClockMock)

    val date = LocalDateTime.now()
    umClockMock.now returns date

    val captor = capture[AuthToken]
    val uuid = UUID.randomUUID().toString

    authTokenService.create(uuid, 2 minutes)

    there was one(authTokenDAOMock).save(captor)
    val argument = captor.value

    argument.expiry must be equalTo date.plusSeconds((2 minutes).toSeconds) and
      (argument.userID.toString must be equalTo uuid)
  }

  def e2 = {
    val authTokenDAOMock: AuthTokenDAO = mock[AuthTokenDAO]
    val umClockMock: UMClock = mock[UMClock]

    val authTokenService: AuthTokenServiceImpl = new AuthTokenServiceImpl(authTokenDAOMock, umClockMock)

    val date = LocalDateTime.now()
    umClockMock.now returns date

    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()

    val token = AuthToken(uuid1, UUID.randomUUID(), umClockMock.now)

    authTokenDAOMock.find(uuid2) returns Future.successful(Some(token))
    authTokenDAOMock.find(uuid1) returns Future.successful(None)

    (authTokenService.validate(uuid1).value.get.get must beEmpty) and
      (authTokenService.validate(uuid2).value.get.get must not beEmpty)
  }

  def e3 = {
    val authTokenDAOMock: AuthTokenDAO = mock[AuthTokenDAO]
    val umClockMock: UMClock = mock[UMClock]

    val authTokenService: AuthTokenServiceImpl = new AuthTokenServiceImpl(authTokenDAOMock, umClockMock)

    val date = LocalDateTime.now()
    umClockMock.now returns date

    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()

    val token1 = AuthToken(uuid1, UUID.randomUUID(), umClockMock.now)
    val token2 = AuthToken(uuid2, UUID.randomUUID(), umClockMock.now)

    authTokenDAOMock.remove(uuid1) returns Future.successful(())
    authTokenDAOMock.remove(uuid2) returns Future.successful(())

    authTokenDAOMock.findExpired(date) returns Future.successful(List(token1, token2))

    val result = Await.result(authTokenService.clean, 5 seconds)

    result must have size 2
  }
}
