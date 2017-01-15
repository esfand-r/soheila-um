package com.soheila.um.vos

import play.api.libs.json.Json

/**
 * The form data.
 *
 * @param password to reset.
 */
case class ResetPasswordVO(
  password: String
)

/**
 * The companion object.
 */
object ResetPasswordVO {

  /**
   * Converts the [Date] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ResetPasswordVO]
}
