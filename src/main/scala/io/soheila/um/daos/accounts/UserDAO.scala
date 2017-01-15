package io.soheila.um.daos.accounts

import com.mohiva.play.silhouette.api.LoginInfo
import io.soheila.um.entities.User
import io.soheila.um.exceptions.UMDAOException
import io.soheila.commons.crud.CRUDDAO
import io.soheila.commons.exceptions.MongoDAOException

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO extends CRUDDAO[User, String] {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Soft delete a user.
   *
   * @param uuid  UUID of user to be soft deleted.
   * @return The boolean indicating success of operation of exception in case of failure..
   */
  def archive(uuid: String): Future[Either[UMDAOException, Boolean]]

  /**
   * Restore a soft deleted a user.
   *
   * @param uuid  UUID of user to be restored.
   * @return The boolean indicating success of operation of exception in case of failure..
   */
  def restore(uuid: String): Future[Either[UMDAOException, Boolean]]
}
