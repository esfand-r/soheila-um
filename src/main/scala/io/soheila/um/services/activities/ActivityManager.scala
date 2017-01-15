package io.soheila.um.services.activities

import java.time.LocalDateTime
import javax.inject.Inject

import akka.actor.{ Actor, ActorLogging }
import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.types.DeviceType.DeviceType
import io.soheila.um.utils.ActivityTypeToEventMapper
import play.api.libs.concurrent.Execution.Implicits._

case class UserActivityEvent(
  userUUID: String,
  activityType: ActivityType,
  userIP: String,
  userDevice: DeviceType,
  timestamp: LocalDateTime
)

class ActivityManager @Inject() (
    userActivityService: UserActivityService
) extends Actor with ActorLogging {
  override def receive: Receive = {
    case UserActivityEvent(userUUID, activityType, userIP, userDevice, _) => {
      userActivityService.insert(userUUID, activityType, userIP, userDevice).map {
        case success: Boolean if success =>
          val event = ActivityTypeToEventMapper.mapActivity(userUUID, activityType)
          context.system.eventStream.publish(event)
        case _ => log.error(s"Insertion of activity with type=$activityType for user with uuid=$userUUID failed")
      }
    }
  }
}
