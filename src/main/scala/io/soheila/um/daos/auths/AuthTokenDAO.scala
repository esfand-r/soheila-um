package io.soheila.um.daos.auths

import java.time.LocalDateTime
import java.util.UUID

import io.soheila.um.entities.AuthToken

import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 * This token can be used for user activation or password reset where a one time token is required.
 */
trait AuthTokenDAO {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]]

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: LocalDateTime): Future[Seq[AuthToken]]

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken]

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit]

  def clear(): Future[Unit]
}
