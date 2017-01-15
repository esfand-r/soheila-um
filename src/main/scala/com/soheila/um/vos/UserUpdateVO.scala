package com.soheila.um.vos

import com.soheila.um.entities.User
import com.soheila.um.types.UserRole

case class UserUpdateVO(
    firstName: String,
    middleName: String = "",
    lastName: String,
    email: String,
    avatarURL: Option[String],
    roles: Seq[UserRole.Value],
    activated: Boolean
) {
  def toUser(user: User): User = {
    user.copy(
      firstName = Some(firstName),
      middleName = Some(middleName),
      lastName = Some(lastName),
      fullName = Some(firstName + " " + middleName + " " + lastName),
      email = Some(email),
      avatarURL = avatarURL,
      roles = if (roles.nonEmpty) roles.toSet else Set(UserRole.SimpleUser),
      activated = activated
    )
  }
}

