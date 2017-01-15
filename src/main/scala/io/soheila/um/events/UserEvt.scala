package io.soheila.um.events

sealed trait UserEvt {
  def userUUID: String
}

case class UserLoggedIn(override val userUUID: String) extends UserEvt
case class UserLoggedOut(override val userUUID: String) extends UserEvt
case class UserCreated(override val userUUID: String) extends UserEvt
case class UserUpdated(override val userUUID: String) extends UserEvt
case class UserArchived(override val userUUID: String) extends UserEvt
case class UserDeleted(override val userUUID: String) extends UserEvt
case class UserActivated(override val userUUID: String) extends UserEvt
case class PasswordChanged(override val userUUID: String) extends UserEvt
case class PasswordReset(override val userUUID: String) extends UserEvt
