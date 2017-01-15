package io.soheila.um.services.activities

import com.google.inject.Inject
import io.soheila.um.daos.activities.UserActivityDAO
import io.soheila.um.entities.UserActivity
import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.types.DeviceType.DeviceType
import io.soheila.um.utils.UMClock
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
class MongoUserActivityService @Inject() (userActivityDAO: UserActivityDAO, clock: UMClock) extends UserActivityService {
  val logger = play.api.Logger(this.getClass)

  /**
   * Insert user activity log.
   *
   * @param userUUID The unique identifier of user.
   * @return boolean indicating success or failure of operation.
   */
  override def insert(userUUID: String, activityType: ActivityType, userIP: String, userDevice: DeviceType): Future[Boolean] = {
    val activity = UserActivity(userUUID, activityType, userIP, userDevice, clock.now)
    userActivityDAO.create(activity).map {
      case Left(dAOException) =>
        logger.error(dAOException.message, dAOException)
        false
      case Right(s) => true
    }
  }
}
