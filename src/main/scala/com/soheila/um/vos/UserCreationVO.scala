package com.soheila.um.vos

import com.mohiva.play.silhouette.api.LoginInfo
import com.soheila.um.entities.User
import com.soheila.um.types.UserRole
import play.api.libs.json.Json

case class UserCreationVO(
    uuid: String,
    loginInfo: LoginInfo,
    firstName: String,
    middleName: String = "",
    lastName: String,
    email: String,
    avatarURL: Option[String],
    roles: Seq[UserRole.Value],
    activated: Boolean
) {
  def toUser: User = {
    User(
      uuid = None,
      loginInfo = loginInfo,
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

object UserCreationVO {
  implicit val jsonFormat = Json.format[UserCreationVO]
}
