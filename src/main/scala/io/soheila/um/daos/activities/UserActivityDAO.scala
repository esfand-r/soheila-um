package io.soheila.um.daos.activities

import io.soheila.um.entities.UserActivity
import io.soheila.um.vos.activities.UserActivityQuery
import io.soheila.commons.crud.CRUDDAO
import io.soheila.commons.entities.Page
import io.soheila.commons.exceptions.MongoDAOException

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserActivityDAO extends CRUDDAO[UserActivity, String] {
  def find(userActivityQuery: UserActivityQuery, page: Int, limit: Int, sortFilter: Option[(String, Int)]): Future[Either[MongoDAOException, Page[UserActivity]]]
}
