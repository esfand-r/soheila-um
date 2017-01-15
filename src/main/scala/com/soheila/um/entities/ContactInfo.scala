package com.soheila.um.entities

import play.api.libs.json.Json

case class ContactInfo(
  name: String,
  street: String,
  city: String,
  state: String,
  country: String,
  postcode: String,
  phoneNumbers: Set[String],
  isPrimary: Boolean = false,
  isBusiness: Boolean = false
)

object ContactInfo {
  implicit val jsonFormat = Json.format[ContactInfo]
}
