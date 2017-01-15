package io.soheila.um.daos.activities

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.um.entities.UserActivity
import io.soheila.um.types.ActivityType
import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.vos.activities.UserActivityQuery
import io.soheila.um.{ MongoScope, MongoSpecification, WithMongo }
import play.api.test.{ PlaySpecification, WithServer }

import scala.concurrent.ExecutionContext

class UserActivityDaoSpec(implicit ec: ExecutionContext) extends PlaySpecification with MongoSpecification {

  "The 'find' method" should {
    "find activities by type" in new WithMongo with Context {
      val userUUID = UUID.randomUUID().toString

      val activity1 = UserActivity(userUUID, ActivityType.Logout, "ip")
      val activity2 = UserActivity(userUUID, ActivityType.Login, "ip2")
      val activity3 = UserActivity(userUUID, ActivityType.PasswordReset, "ip3")

      await(userActivityDao.create(activity1))
      await(userActivityDao.create(activity2))
      await(userActivityDao.create(activity3))

      val useractivityQuery = UserActivityQuery(activityType = Some(ActivityType.Login))

      val activities = await(userActivityDao.find(useractivityQuery, 0, 10, None)).right.get

      activities.items.size must beEqualTo(1)
      activities.items.head.activityType must beEqualTo(ActivityType.Login)
      activities.items.head.userIP must beEqualTo("ip2")
    }

    "find activities by user's UUID" in new WithMongo with Context {
      val userUUID = UUID.randomUUID().toString
      val user2UUID = UUID.randomUUID().toString
      val user3UUID = UUID.randomUUID().toString

      val activity1 = UserActivity(userUUID, ActivityType.Logout, "ip")
      val activity2 = UserActivity(user2UUID, ActivityType.Login, "ip2")
      val activity3 = UserActivity(user2UUID, ActivityType.Login, "ip2")
      val activity4 = UserActivity(user3UUID, ActivityType.PasswordReset, "ip3")

      await(userActivityDao.create(activity1))
      await(userActivityDao.create(activity2))
      await(userActivityDao.create(activity3))
      await(userActivityDao.create(activity4))

      val useractivityQuery = UserActivityQuery(userUUID = Some(user3UUID))

      val activities = await(userActivityDao.find(useractivityQuery, 0, 10, None)).right.get

      activities.items.size must beEqualTo(1)
      activities.items.head.activityType must beEqualTo(ActivityType.PasswordReset)
      activities.items.head.userIP must beEqualTo("ip3")
    }

    "find activities after the specified date inclusive" in new WithMongo with Context {
      val dateString1 = "2017-03-02T11:58:51.386"
      val date1 = LocalDateTime.parse(dateString1)

      val dateString2 = "2017-03-05T11:58:51.386"
      val date2 = LocalDateTime.parse(dateString2)

      val dateString3 = "2017-03-09T11:58:51.386"
      val date3 = LocalDateTime.parse(dateString3)

      val dateString4 = "2017-03-18T11:58:51.386"
      val date4 = LocalDateTime.parse(dateString4)

      val userUUID = UUID.randomUUID().toString
      val user2UUID = UUID.randomUUID().toString
      val user3UUID = UUID.randomUUID().toString

      val activity1 = UserActivity(userUUID = userUUID, activityType = ActivityType.Logout, userIP = "ip", timestamp = date1)
      val activity2 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date2)
      val activity3 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date3)
      val activity4 = UserActivity(userUUID = user3UUID, activityType = ActivityType.PasswordReset, userIP = "ip3", timestamp = date4)

      await(userActivityDao.create(activity1))
      await(userActivityDao.create(activity2))
      await(userActivityDao.create(activity3))
      await(userActivityDao.create(activity4))

      val useractivityQuery = UserActivityQuery(laterThan = Some(date2))

      val activities = await(userActivityDao.find(useractivityQuery, 0, 10, None)).right.get

      activities.items.size must beEqualTo(3)
    }

    "find activities before the specified date inclusive" in new WithMongo with Context {
      val dateString1 = "2017-03-02T11:58:51.386"
      val date1 = LocalDateTime.parse(dateString1)

      val dateString2 = "2017-03-05T11:58:51.386"
      val date2 = LocalDateTime.parse(dateString2)

      val dateString3 = "2017-03-09T11:58:51.386"
      val date3 = LocalDateTime.parse(dateString3)

      val dateString4 = "2017-03-18T11:58:51.386"
      val date4 = LocalDateTime.parse(dateString4)

      val userUUID = UUID.randomUUID().toString
      val user2UUID = UUID.randomUUID().toString
      val user3UUID = UUID.randomUUID().toString

      val activity1 = UserActivity(userUUID = userUUID, activityType = ActivityType.Logout, userIP = "ip", timestamp = date1)
      val activity2 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date2)
      val activity3 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date3)
      val activity4 = UserActivity(userUUID = user3UUID, activityType = ActivityType.PasswordReset, userIP = "ip3", timestamp = date4)

      await(userActivityDao.create(activity1))
      await(userActivityDao.create(activity2))
      await(userActivityDao.create(activity3))
      await(userActivityDao.create(activity4))

      val useractivityQuery = UserActivityQuery(earlierThan = Some(date3))

      val activities = await(userActivityDao.find(useractivityQuery, 0, 10, None)).right.get

      activities.items.size must beEqualTo(3)
    }

    "find activities between the specified dates inclusive" in new WithMongo with Context {
      val dateString1 = "2017-03-02T11:58:51.386"
      val date1 = LocalDateTime.parse(dateString1)

      val dateString2 = "2017-03-05T11:58:51.386"
      val date2 = LocalDateTime.parse(dateString2)

      val dateString3 = "2017-03-09T11:58:51.386"
      val date3 = LocalDateTime.parse(dateString3)

      val dateString4 = "2017-03-18T11:58:51.386"
      val date4 = LocalDateTime.parse(dateString4)

      val userUUID = UUID.randomUUID().toString
      val user2UUID = UUID.randomUUID().toString
      val user3UUID = UUID.randomUUID().toString

      val activity1 = UserActivity(userUUID = userUUID, activityType = ActivityType.Logout, userIP = "ip", timestamp = date1)
      val activity2 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date2)
      val activity3 = UserActivity(userUUID = user2UUID, activityType = ActivityType.Login, userIP = "ip2", timestamp = date3)
      val activity4 = UserActivity(userUUID = user3UUID, activityType = ActivityType.PasswordReset, userIP = "ip3", timestamp = date4)

      await(userActivityDao.create(activity1))
      await(userActivityDao.create(activity2))
      await(userActivityDao.create(activity3))
      await(userActivityDao.create(activity4))

      val useractivityQuery = UserActivityQuery(laterThan = Some(date2), earlierThan = Some(date3))

      val activities = await(userActivityDao.find(useractivityQuery, 0, 10, None)).right.get

      activities.items.size must beEqualTo(2)
    }
  }

  /**
   * The context.
   */
  trait Context extends MongoScope {
    self: WithServer =>

    lazy val userActivityDao = new MongoUserActivityDAO(reactiveMongoAPI)
  }
}
