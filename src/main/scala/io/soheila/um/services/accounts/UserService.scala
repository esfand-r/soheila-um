package io.soheila.um.services.accounts

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import io.soheila.um.entities.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param uuid The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(uuid: String): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]

  /**
   * Updates a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  def update(user: User): Future[User]

  /**
   * Soft delete a user.
   *
   * @param uuid  UUID of user to be soft deleted.
   * @return The updated user.
   */
  def archive(uuid: String): Future[Boolean]
}
