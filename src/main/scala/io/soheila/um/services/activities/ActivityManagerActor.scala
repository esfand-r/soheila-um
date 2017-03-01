package io.soheila.um.services.activities

import java.time.LocalDateTime
import javax.inject.Inject

import akka.actor.Actor
import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.types.DeviceType.DeviceType
import io.soheila.um.utils.ActivityUserEventMapper
import play.api.libs.concurrent.Execution.Implicits._

case class UserActivityEvent(
  userUUID: String,
  activityType: ActivityType,
  userIP: String,
  userDevice: DeviceType,
  timestamp: LocalDateTime
)

class ActivityManagerActor @Inject() (
    userActivityService: UserActivityService
) extends Actor {
  override def receive: Receive = {
    case UserActivityEvent(userUUID, activityType, userIP, userDevice, _) => {
      userActivityService.insert(userUUID, activityType, userIP, userDevice).map {
        case success: Boolean if success =>
          val event = ActivityUserEventMapper.mapActivity(userUUID, activityType)
          context.system.eventStream.publish(event)
          sender ! success
        case _ => sender ! false
      }
    }
  }
}
