package io.soheila.um.types

import play.api.libs.json.{ Format, JsString, JsSuccess, JsValue }

object NotificationMethod extends Enumeration {
  type NotificationMethod = Value
  val Email, SMS = Value

  implicit val notificationMethodEnumFormat = new Format[NotificationMethod.Value] {
    def reads(json: JsValue) = JsSuccess(NotificationMethod.withName(json.as[String]))

    def writes(notificationMethod: NotificationMethod.Value) = JsString(notificationMethod.toString)
  }
}

