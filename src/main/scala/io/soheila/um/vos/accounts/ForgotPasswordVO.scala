package io.soheila.um.vos.accounts

import play.api.libs.json.Json

/**
 * The form data.
 *
 * @param email email address to use to send link to reset password.
 */
case class ForgotPasswordVO(
  email: String
)

/**
 * The companion object.
 */
object ForgotPasswordVO {

  /**
   * Converts the [Date] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ForgotPasswordVO]
}
