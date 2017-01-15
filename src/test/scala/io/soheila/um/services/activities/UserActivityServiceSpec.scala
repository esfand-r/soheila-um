package io.soheila.um.services.activities

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.commons.exceptions.{ ErrorCode, MongoDAOException }
import io.soheila.um.daos.activities.UserActivityDAO
import io.soheila.um.entities.UserActivity
import io.soheila.um.types.{ ActivityType, DeviceType }
import io.soheila.um.utils.UMClock
import org.specs2.Specification
import org.specs2.mock.Mockito

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class UserActivityServiceSpec extends Specification with Mockito {
  def is = sequential ^ s2"""
    'insert' method should return false if an exception occurs during insertion into mongo Fails     $e1
    'insert' method should return true if no issue happens creating the entity in DB                 $e2
                                                        """
  val mongoAuthDAOMock = mock[UserActivityDAO]
  val umClockMock: UMClock = mock[UMClock]

  val date = LocalDateTime.now()

  umClockMock.now returns date

  val userActivityService = new MongoUserActivityService(mongoAuthDAOMock, umClockMock)

  def e1 = {
    val userUuid = UUID.randomUUID().toString

    val activity = UserActivity(None, userUuid, ActivityType.Activate, "ip", DeviceType.Mobile, date)

    val e = MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)
    val res: Either[MongoDAOException, UserActivity] = Left(e)

    mongoAuthDAOMock.create(activity) returns Future.successful(res)

    val response = Await.result(userActivityService.insert(userUuid, ActivityType.Activate, "ip", DeviceType.Mobile), 5 seconds)
    response must beFalse
  }

  def e2 = {
    val userUuid = UUID.randomUUID().toString

    val activity = UserActivity(None, userUuid, ActivityType.Activate, "ip", DeviceType.Mobile, date)

    val res: Either[MongoDAOException, UserActivity] = Right(activity)

    mongoAuthDAOMock.create(activity) returns Future.successful(res)

    val response = Await.result(userActivityService.insert(userUuid, ActivityType.Activate, "ip", DeviceType.Mobile), 5 seconds)
    response must beTrue
  }
}
