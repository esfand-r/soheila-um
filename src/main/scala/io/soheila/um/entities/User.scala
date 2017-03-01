package io.soheila.um.entities

import java.time.{ Clock, LocalDateTime }
import java.util.Locale

import com.fasterxml.uuid.Generators
import play.api.libs.json.Json
import com.mohiva.play.silhouette.api.LoginInfo
import io.soheila.commons.entities.{ Attribute, IdentityWithAudit, Locatable }
import io.soheila.commons.geospatials.Coordinate
import io.soheila.um.types.UserRole

/**
 * The user object.
 *
 * @param uuid      The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName  Maybe the last name of the authenticated user.
 * @param fullName  Maybe the full name of the authenticated user.
 * @param email     Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param activated Indicates that the user has activated its registration.
 */
case class User(
    uuid: Option[String],
    loginInfo: LoginInfo,
    firstName: Option[String],
    middleName: Option[String] = Some(""),
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String],
    activated: Boolean = false,
    createdOn: Option[LocalDateTime] = None,
    updatedOn: Option[LocalDateTime] = None,
    roles: Set[UserRole.Value] = Set(UserRole.SimpleUser),
    archived: Boolean = false,
    override val coordinate: Coordinate = Coordinate(0, 0),
    contactInfo: Option[ContactInfo] = None,
    notificationSettings: NotificationSettings = NotificationSettings(),
    additionalContactInfo: Set[ContactInfo] = Set(),
    attributes: Seq[Attribute] = Seq(),
    preferredLocale: String = Locale.getDefault.toLanguageTag

) extends Locatable with SilhouetteIdentity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name = fullName.orElse {
    firstName -> lastName match {
      case (Some(f), Some(l)) => Some(f + " " + l)
      case (Some(f), None) => Some(f)
      case (None, Some(l)) => Some(l)
      case _ => None
    }
  }
}

/**
 * The companion object.
 */
object User {

  implicit object StoryIdentity extends IdentityWithAudit[User, String] {
    val name = "uuid"

    override def of(entity: User): Option[String] = entity.uuid

    override def set(entity: User, uuid: String): User = entity.copy(uuid = Option(uuid))

    override def clear(entity: User): User = entity.copy(uuid = None)

    override def newID: String = Generators.timeBasedGenerator().generate().toString

    override def addAuditTrail(entity: User): User = entity.copy(createdOn = Some(LocalDateTime.now()), updatedOn = Some(LocalDateTime.now()))

    override def updateAuditTrail(entity: User): User = entity.copy(updatedOn = Some(LocalDateTime.now()))
  }

  /**
   * Converts the [User] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[User]

}
