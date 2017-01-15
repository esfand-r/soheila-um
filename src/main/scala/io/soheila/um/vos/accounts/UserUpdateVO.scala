//package io.soheila.um.vos.accounts
//
//import java.util.Locale
//
//import io.soheila.um.entities.User
//import io.soheila.um.types.UserRole
//
//case class UserUpdateVO(
//    firstName: String,
//    middleName: String = "",
//    lastName: String,
//    email: String,
//    avatarURL: Option[String],
//    roles: Seq[UserRole.Value],
//    activated: Boolean,
//    preferredLocale: String = Locale.getDefault.toLanguageTag
//) {
//  def toUser(user: User): User = {
//    user.copy(
//      firstName = Some(firstName),
//      middleName = Some(middleName),
//      lastName = Some(lastName),
//      fullName = Some(firstName + " " + middleName + " " + lastName),
//      email = Some(email),
//      avatarURL = avatarURL,
//      roles = if (roles.nonEmpty) roles.toSet else Set(UserRole.SimpleUser),
//      activated = activated
//    )
//  }
//}
//
