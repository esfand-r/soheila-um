package com.soheila.um.vos

import play.api.libs.json.Json

/**
 * The form data.
 *
 * @param currentPassword Current password to be changed.
 * @param newPassword        target password.
 */
case class ChangePasswordVO(
  currentPassword: String,
  newPassword: String
)

/**
 * The companion object.
 */
object ChangePasswordVO {

  /**
   * Converts the [Date] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ChangePasswordVO]
}
