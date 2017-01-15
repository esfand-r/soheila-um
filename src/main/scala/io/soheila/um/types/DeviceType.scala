package io.soheila.um.types

import play.api.libs.json.{ Format, JsString, JsSuccess, JsValue }

object DeviceType extends Enumeration {
  type DeviceType = Value
  val Mobile, PC, Unknown = Value

  implicit val rolesEnumFormat = new Format[DeviceType.Value] {
    def reads(json: JsValue) = JsSuccess(DeviceType.withName(json.as[String]))
    def writes(role: DeviceType.Value) = JsString(role.toString)
  }
}

