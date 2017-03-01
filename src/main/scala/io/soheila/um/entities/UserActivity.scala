package io.soheila.um.entities

import java.time.LocalDateTime

import com.fasterxml.uuid.Generators
import io.soheila.um.types.ActivityType.ActivityType
import io.soheila.um.types.DeviceType
import io.soheila.um.types.DeviceType.DeviceType
import io.soheila.commons.entities.Identity
import play.api.libs.json.Json

case class UserActivity(
  uuid: Option[String],
  userUUID: String,
  activityType: ActivityType,
  userIP: String,
  userDevice: DeviceType,
  timestamp: LocalDateTime
)

object UserActivity {
  implicit val jsonFormat = Json.format[UserActivity]

  implicit object UserActivityIdentity extends Identity[UserActivity, String] {
    val name = "uuid"

    override def of(entity: UserActivity): Option[String] = entity.uuid

    override def set(entity: UserActivity, uuid: String): UserActivity = entity.copy(uuid = Option(uuid))

    override def clear(entity: UserActivity): UserActivity = entity.copy(uuid = None)

    override def newID: String = Generators.timeBasedGenerator().generate().toString
  }

  def apply(
    userUUID: String,
    activityType: ActivityType,
    userIP: String,
    userDevice: DeviceType = DeviceType.Unknown,
    timestamp: LocalDateTime = LocalDateTime.now()
  ): UserActivity = {
    UserActivity(None, userUUID, activityType, userIP, userDevice, timestamp)
  }
}
