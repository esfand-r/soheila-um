package io.soheila.um.daos.activities

import javax.inject.Inject

import io.soheila.commons.crud.MongoCRUDDAO
import io.soheila.commons.entities.Page
import io.soheila.commons.exceptions.MongoDAOException
import io.soheila.um.entities.UserActivity
import io.soheila.um.vos.activities.UserActivityQuery
import play.api.libs.json.{ JsObject, Json }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the user object.
 */
class MongoUserActivityDAO @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit override val ec: ExecutionContext)
    extends MongoCRUDDAO[UserActivity, String] with UserActivityDAO {

  override def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("user_activity"))

  override def find(userActivityQuery: UserActivityQuery, page: Int, limit: Int, sortFilter: Option[(String, Int)]): Future[Either[MongoDAOException, Page[UserActivity]]] = {
    Json.toJson(userActivityQuery)
    find(Json.toJson(userActivityQuery).as[JsObject], page, limit, sortFilter)
  }

  override def indexSet: Set[Index] = Set(
    Index(Seq("userUUID" -> IndexType.Ascending)),
    Index(Seq("activityType" -> IndexType.Ascending)),
    Index(Seq("userIP" -> IndexType.Ascending)),
    Index(Seq("userDevice" -> IndexType.Ascending)),
    Index(Seq("timestamp" -> IndexType.Ascending))
  )
}
