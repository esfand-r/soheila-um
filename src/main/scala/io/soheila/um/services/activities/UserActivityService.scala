package io.soheila.um.services.activities

import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.types.DeviceType.DeviceType

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserActivityService {
  /**
   * Insert user activity log.
   *
   * @param userUUID The unique identifier of user.
   * @return boolean indicating success or failure of operation.
   */
  def insert(userUUID: String, activityType: ActivityType, userIP: String, userDevice: DeviceType): Future[Boolean]
}
