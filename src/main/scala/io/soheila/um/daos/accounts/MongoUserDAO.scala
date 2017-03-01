package io.soheila.um.daos.accounts

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import io.soheila.um.entities.User
import io.soheila.um.exceptions.DAOException
import io.soheila.commons.crud.MongoCRUDDAO
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.api.indexes.IndexType.Geo2DSpherical
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ ExecutionContext, Future }

/**
 * DAO for User entity in MongoDB.
 */
class MongoUserDAO @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit override val ec: ExecutionContext)
    extends MongoCRUDDAO[User, String] with UserDAO {

  override def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    collection.flatMap(_.find(Json.obj("loginInfo" -> loginInfo)).one[User])
  }

  override def archive(uuid: String): Future[Either[DAOException, Boolean]] = {
    archive(uuid, archived = true)
  }

  override def restore(uuid: String): Future[Either[DAOException, Boolean]] = {
    archive(uuid, archived = false)
  }

  private def archive(uuid: String, archived: Boolean): Future[Either[DAOException, Boolean]] = {
    collection.flatMap(col => {
      val updateOp = col.updateModifier(Json.obj("$set" -> Json.obj("archived" -> archived)), fetchNewObject = true)

      col.findAndModify(Json.obj("uuid" -> uuid), updateOp) map {
        case le if le.value.isDefined => Right(true)
        case le if le.lastError.isDefined => Left(DAOException(le.lastError.get.err.getOrElse("Unknown Error"), null))
        case _ => Left(DAOException("Unknown Error", null))
      }
    })
  }

  override def indexSet: Set[Index] = Set(
    Index(Seq("uuid" -> IndexType.Ascending), unique = true),
    Index(Seq("email" -> IndexType.Ascending), unique = true),
    Index(Seq("roles" -> IndexType.Ascending)),
    Index(List("firstName" -> IndexType.Ascending, "lastName" -> IndexType.Ascending, "fullName" -> IndexType.Text)),
    Index(Seq("createdOn" -> IndexType.Descending, "updatedOn" -> IndexType.Descending)),
    Index(Seq(("coordinate", Geo2DSpherical)), Some("geo2DSphericalIdx")),
    Index(List("attributes.key" -> IndexType.Ascending, "attributes.value" -> IndexType.Ascending))
  )
}
