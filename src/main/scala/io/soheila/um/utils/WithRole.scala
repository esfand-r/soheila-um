package io.soheila.um.utils

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import io.soheila.um.entities.User
import io.soheila.um.types.UserRole
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRole(role: UserRole.Value) extends Authorization[User, JWTAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: JWTAuthenticator)(implicit request: Request[B]): Future[Boolean] = {
    Future.successful(user.roles match {
      case list: Set[UserRole.Value] => list.contains(role)
      case _ => false
    })
  }
}
