package io.soheila.um.jobs

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import io.soheila.um.entities.AuthToken
import io.soheila.um.jobs.AuthTokenCleaner.Clean
import io.soheila.um.services.auths.AuthTokenService
import io.soheila.um.utils.UMClock
import org.specs2.SpecificationLike
import org.specs2.mock.Mockito
import org.specs2.specification.AfterAll
import org.specs2.specification.core.SpecStructure

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.testkit._

class AuthTokenCleanerSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito with ImplicitSender with AfterAll {
  override def is: SpecStructure =
    s2"""
      should request token clean up when receiving Clean message $e1
    """

  10.seconds.dilated

  val authTokenServiceMock = mock[AuthTokenService]
  val clockMock = mock[UMClock]

  val actor = system.actorOf(Props(new AuthTokenCleaner(authTokenServiceMock, clockMock)))

  def e1 = {
    val authToken = AuthToken(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now())
    authTokenServiceMock.clean returns Future.successful(Seq(authToken))
    clockMock.now returns LocalDateTime.now

    actor ! Clean

    Thread.sleep(1000)

    there was one(authTokenServiceMock).clean
  }

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }
}
