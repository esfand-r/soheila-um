package io.soheila.um.vos.accounts

import play.api.libs.json.Json

/**
 * The form data.
 *
 * @param firstName The first name of a user.
 * @param lastName The last name of a user.
 * @param email The email of the user.
 * @param password The password of the user.
 */
case class SignUpVO(
  firstName: String,
  lastName: String,
  email: String,
  password: String
)

/**
 * The companion object.
 */
object SignUpVO {

  /**
   * Converts the [Date] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[SignUpVO]
}
