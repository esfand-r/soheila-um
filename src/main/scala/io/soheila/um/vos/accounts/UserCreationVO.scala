package io.soheila.um.vos.accounts

import java.util.Locale

import com.mohiva.play.silhouette.api.LoginInfo
import io.soheila.um.entities.User
import io.soheila.um.types.UserRole
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
    activated: Boolean,
    preferredLocale: String = Locale.getDefault.toLanguageTag
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
      activated = activated,
      preferredLocale = preferredLocale
    )
  }
}

object UserCreationVO {
  implicit val jsonFormat = Json.format[UserCreationVO]
}
