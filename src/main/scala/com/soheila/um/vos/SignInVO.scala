package com.soheila.um.vos

import play.api.libs.json.Json

/**
 * The form data.
 *
 * @param email The email of the user.
 * @param password The password of the user.
 * @param rememberMe Indicates if the user should stay logged in on the next visit.
 */
case class SignInVO(
  email: String,
  password: String,
  rememberMe: Option[Boolean] = Some(true)
)

/**
 * The companion object.
 */
object SignInVO {

  /**
   * Converts the [Date] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[SignInVO]
}
