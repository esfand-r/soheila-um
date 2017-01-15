package com.soheila.um.entities

import com.soheila.um.types.NotificationMethod
import play.api.libs.json.Json

case class NotificationSettings(
  notificationMethod: NotificationMethod.Value = NotificationMethod.Email,
  notifyWhenProfileUpdated: Boolean = false,
  notifyWhenLoggedInFromNewDevice: Boolean = false
)

object NotificationSettings {
  implicit val jsonFormat = Json.format[NotificationSettings]
}
