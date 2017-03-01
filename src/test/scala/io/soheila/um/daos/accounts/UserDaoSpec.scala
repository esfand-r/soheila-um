package io.soheila.um.daos.accounts

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import io.soheila.um.entities.User
import io.soheila.um.{ MongoScope, MongoSpecification, WithMongo }
import play.api.libs.json.Json
import play.api.test.{ PlaySpecification, WithServer }

import scala.concurrent.ExecutionContext

class UserDaoSpec(implicit ec: ExecutionContext) extends PlaySpecification with MongoSpecification {
  "The 'findByLoginInfo' method" should {
    val loginInfo1 = LoginInfo("providerId1", "providerKey1")
    val user1 = User(
      uuid = None,
      loginInfo = loginInfo1,
      firstName = Some("firstName1"),
      lastName = Some("lastName1"),
      fullName = Some("firstName1" + " " + "lastName1"),
      email = Some("firstName1+lastName1@gmail.com"),
      avatarURL = None,
      activated = true
    )

    val loginInfo2 = LoginInfo("providerId2", "providerKey2")
    val user2 = User(
      uuid = None,
      loginInfo = loginInfo2,
      firstName = Some("firstName2"),
      lastName = Some("lastName2"),
      fullName = Some("firstName2" + " " + "lastName2"),
      email = Some("firstName2+lastName2@gmail.com"),
      avatarURL = None,
      activated = true
    )

    "find and return a user by loginInfo" in new WithMongo with Context {

      await(userDAO.create(user1))
      await(userDAO.create(user2))

      val user: Option[User] = await(userDAO.find(loginInfo2))

      user.get.firstName.get must beEqualTo("firstName2")
      user.get.lastName.get must beEqualTo("lastName2")
      user.get.email.get must beEqualTo("firstName2+lastName2@gmail.com")
    }

    "find and return an empty option when find by loginInfo that doesn't exist" in new WithMongo with Context {

      await(userDAO.create(user1))
      await(userDAO.create(user2))

      val user: Option[User] = await(userDAO.find(LoginInfo("x", "x")))

      user must beEmpty
    }
  }

  "The 'archive' method" should {
    "set the archived flag to true in the user entity" in new WithMongo with Context {

      val loginInfo1 = LoginInfo("providerId1", "providerKey1")
      val user1 = User(
        uuid = None,
        loginInfo = loginInfo1,
        firstName = Some("firstName1"),
        lastName = Some("lastName1"),
        fullName = Some("firstName1" + " " + "lastName1"),
        email = Some("firstName1+lastName1@gmail.com"),
        avatarURL = None,
        activated = true
      )

      val user = await(userDAO.create(user1)).right.get
      user.archived must beFalse

      await(userDAO.archive(user.uuid.get))

      val userAfterArchive = await(userDAO.read(user.uuid.get)).get

      userAfterArchive.archived must beTrue
    }
  }

  "The 'restore' method" should {
    "set the archived flag to false in the user entity" in new WithMongo with Context {

      val loginInfo1 = LoginInfo("providerId1", "providerKey1")
      val user1 = User(
        uuid = None,
        loginInfo = loginInfo1,
        firstName = Some("firstName1"),
        lastName = Some("lastName1"),
        fullName = Some("firstName1" + " " + "lastName1"),
        email = Some("firstName1+lastName1@gmail.com"),
        avatarURL = None,
        activated = true
      )

      val user = await(userDAO.create(user1)).right.get

      await(userDAO.archive(user.uuid.get))

      await(userDAO.restore(user.uuid.get))

      val userAfterRestore = await(userDAO.read(user.uuid.get)).get

      userAfterRestore.archived must beFalse
    }
  }

  /**
   * The context.
   */
  trait Context extends MongoScope {
    self: WithServer =>

    implicit lazy val format = Json.format[OAuth1Info]

    lazy val userDAO = new MongoUserDAO(reactiveMongoAPI)
  }
}
