package io.soheila.um.utils

import io.soheila.um.events._
import io.soheila.um.types.ActivityType
import io.soheila.um.types.ActivityType.ActivityType

object ActivityTypeToEventMapper {
  def mapActivity(userUUID: String, activityType: ActivityType): UserEvt = {
    activityType match {
      case ActivityType.Activate => UserActivated(userUUID)
      case ActivityType.Archive => UserArchived(userUUID)
      case ActivityType.Create => UserCreated(userUUID)
      case ActivityType.Delete => UserDeleted(userUUID)
      case ActivityType.Login => UserLoggedIn(userUUID)
      case ActivityType.Logout => UserLoggedOut(userUUID)
      case ActivityType.PasswordChange => PasswordChanged(userUUID)
      case ActivityType.PasswordReset => PasswordReset(userUUID)
      case ActivityType.Update => UserUpdated(userUUID)
    }
  }
}
