package io.soheila.um.vos.auths

import org.joda.time.DateTime
import play.api.libs.json._

case class Token(token: String, expiresOn: DateTime)

object Token {
  implicit val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(d: DateTime): JsValue = JsString(d.toString)
  }

  implicit val restFormat = Json.format[Token]
}
