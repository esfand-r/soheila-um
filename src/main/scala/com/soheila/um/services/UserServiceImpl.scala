package com.soheila.um.services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.soheila.um.daos.UserDAO
import com.soheila.um.entities.User
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param uuid The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(uuid: String): Future[Option[User]] = userDAO.read(uuid)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.create(user).map {
    case Left(dAOException) => throw dAOException
    case Right(s) => s
  }

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        val userToUpdate = user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          email = profile.email,
          avatarURL = profile.avatarURL
        )
        userDAO.update(user.uuid.get, userToUpdate).map {
          case Left(dAOException) => throw dAOException
          case Right(s) => userToUpdate
        }
      case None => // Insert a new user
        userDAO.create(User(
          uuid = None,
          loginInfo = profile.loginInfo,
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          email = profile.email,
          avatarURL = profile.avatarURL,
          activated = true
        )).map {
          case Left(dAOException) => throw dAOException
          case Right(s) => s
        }
    }
  }

  /**
   * Updates a user.
   *
   * @param user The user to update.
   * @return The updated user.
   */
  override def update(user: User): Future[User] = {
    userDAO.update(user.uuid.get, user).map {
      case Left(dAOException) => throw dAOException
      case Right(s) => user
    }
  }

  /**
   * Soft delete a user.
   *
   * @param uuid UUID of user to be soft deleted.
   * @return The updated user.
   */
  override def archive(uuid: String): Future[Boolean] = {
    userDAO.archive(uuid).map {
      case Left(dAOException) =>
        //todo: handle error properly.
        false
      case Right(s) => true
    }
  }
}
