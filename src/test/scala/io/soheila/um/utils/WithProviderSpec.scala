package io.soheila.um.utils

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import io.soheila.um.entities.User
import io.soheila.um.types.UserRole
import org.specs2.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.core.SpecStructure
import play.api.mvc.Request

import scala.concurrent.Await
import scala.concurrent.duration._

class WithProviderSpec extends Specification with Mockito {
  override def is: SpecStructure =
    s2"""
        'isAuthorized' should return false when user does not have logininfo containing the provider specified $e1
        'isAuthorized' should return false when user has logininfo containing the provider specified $e2
      """

  val loginInfo1 = LoginInfo("google", "google123")

  val user = User(
    uuid = None,
    loginInfo = loginInfo1,
    firstName = Some("firstName1"),
    lastName = Some("lastName1"),
    fullName = Some("firstName1" + " " + "lastName1"),
    email = Some("firstName1+lastName1@gmail.com"),
    avatarURL = None,
    roles = Set(UserRole.SimpleUser),
    activated = true
  )

  val authenticator = mock[JWTAuthenticator]

  def e1 = {
    val withProvider = WithProvider[JWTAuthenticator]("facebook")

    implicit val request = mock[Request[String]]

    val response = Await.result(withProvider.isAuthorized(user, authenticator), 5 millis)

    response must beFalse
  }

  def e2 = {
    val withProvider = WithProvider[JWTAuthenticator]("google")

    implicit val request = mock[Request[String]]

    val response = Await.result(withProvider.isAuthorized(user, authenticator), 5 millis)

    response must beTrue
  }
}
