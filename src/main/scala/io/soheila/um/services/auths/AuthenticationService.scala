package io.soheila.um.services.auths

import com.mohiva.play.silhouette.api.util.{ Credentials, ExtractableRequest }
import io.soheila.um.entities.AuthToken
import io.soheila.um.vos._
import io.soheila.um.vos.accounts.{ ChangePasswordVO, ResetPasswordVO, SignInVO, SignUpVO }
import io.soheila.um.vos.auths.Token
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Handles actions to authenticate.
 */
trait AuthenticationService {
  def signIn(signInData: SignInVO)(implicit request: RequestHeader): Future[Token]

  def socialSignIn[B](provider: String)(implicit request: ExtractableRequest[B]): Future[Either[Result, String]]

  def signUp(signUpData: SignUpVO)(implicit request: RequestHeader): Future[String]

  def resetPassword(authToken: AuthToken, resetPasswordData: ResetPasswordVO)(implicit request: RequestHeader): Future[Boolean]

  def changePassword(credentials: Credentials, changePasswordData: ChangePasswordVO)(implicit request: RequestHeader): Future[Boolean]
}
