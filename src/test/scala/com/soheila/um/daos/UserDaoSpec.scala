package com.soheila.um.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import com.soheila.um.entities.User
import com.soheila.um.{ MongoScope, MongoSpecification, WithMongo }

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import play.api.test.WithServer
import org.specs2._

import scala.concurrent._

class UserDaoSpec(implicit ec: ExecutionContext) extends mutable.Specification with MongoSpecification {
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

      Await.result(userDAO.create(user1), 5 seconds)
      Await.result(userDAO.create(user2), 5 seconds)

      val user: Option[User] = Await.result(userDAO.find(loginInfo2), 5 seconds)

      user.get.firstName.get must beEqualTo("firstName2")
      user.get.lastName.get must beEqualTo("lastName2")
      user.get.email.get must beEqualTo("firstName2+lastName2@gmail.com")
    }

    "find and return an empty option when find by loginInfo that doesn't exist" in new WithMongo with Context {

      Await.result(userDAO.create(user1), 5 seconds)
      Await.result(userDAO.create(user2), 5 seconds)

      val user: Option[User] = Await.result(userDAO.find(LoginInfo("x", "x")), 5 seconds)

      user must beEmpty
    }
  }

  /**
   * The context.
   */
  trait Context extends MongoScope {
    self: WithServer =>

    implicit lazy val format = Json.format[OAuth1Info]

    lazy val userDAO = new UserDAOImpl(reactiveMongoAPI)
  }
}
