package com.soheila.um.types

import play.api.libs.json.{ Format, JsString, JsSuccess, JsValue }

object UserRole extends Enumeration {
  type UserRoles = Value
  val Guest, Admin, SimpleUser = Value

  implicit val rolesEnumFormat = new Format[UserRole.Value] {
    def reads(json: JsValue) = JsSuccess(UserRole.withName(json.as[String]))
    def writes(role: UserRole.Value) = JsString(role.toString)
  }
}

