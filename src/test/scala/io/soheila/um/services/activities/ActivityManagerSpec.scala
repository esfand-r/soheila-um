package io.soheila.um.services.activities

import akka.actor.{ ActorSystem, Props }
import java.time.LocalDateTime

import akka.testkit.{ ImplicitSender, TestKit }
import io.soheila.um.utils.UMClock
import org.specs2.SpecificationLike
import org.specs2.mock.Mockito
import org.specs2.specification.AfterAll
import org.specs2.specification.core.SpecStructure

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.testkit._
import io.soheila.um.types.{ ActivityType, DeviceType }

class ActivityManagerSpec extends TestKit(ActorSystem()) with SpecificationLike with Mockito with ImplicitSender with AfterAll {
  override def is: SpecStructure =
    s2"""
      should request token clean up when receiving Clean message $e1
    """

  10.seconds.dilated

  val userActivityServiceMock = mock[UserActivityService]
  val clockMock = mock[UMClock]

  val actor = system.actorOf(Props(new ActivityManager(userActivityServiceMock)))

  def e1 = {
    val uuid = "uuid"
    val ip = "ip"
    val date = LocalDateTime.now()

    val event = UserActivityEvent(uuid, ActivityType.Activate, ip, DeviceType.Mobile, date)
    clockMock.now returns date

    userActivityServiceMock.insert(uuid, ActivityType.Activate, ip, DeviceType.Mobile) returns Future.successful(true)

    actor ! event

    Thread.sleep(1000)

    there was one(userActivityServiceMock).insert(uuid, ActivityType.Activate, ip, DeviceType.Mobile)
  }

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }
}
