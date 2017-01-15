package com.soheila.um.forms

import com.soheila.um.vos.SignInVO
import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the credentials.
 */
object SignInForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "rememberMe" -> optional(boolean)
    )(SignInVO.apply)(SignInVO.unapply)
  )
}
