package io.soheila.um.types

import play.api.libs.json.{ Format, JsString, JsSuccess, JsValue }

object ActivityType extends Enumeration {
  type ActivityType = Value
  val Login, PasswordChange, PasswordReset, Logout, Create, Update, Activate, Archive, Delete = Value

  implicit val rolesEnumFormat = new Format[ActivityType.Value] {
    def reads(json: JsValue) = JsSuccess(ActivityType.withName(json.as[String]))
    def writes(role: ActivityType.Value) = JsString(role.toString)
  }
}

