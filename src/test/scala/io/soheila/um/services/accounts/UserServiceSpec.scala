package io.soheila.um.services.accounts

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestProbe
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import io.soheila.commons.exceptions.{ ErrorCode, MongoDAOException }
import io.soheila.um.daos.accounts.UserDAO
import io.soheila.um.entities.{ User, UserActivity }
import io.soheila.um.exceptions.{ UMDAOException, UserServiceException }
import io.soheila.um.types.{ ActivityType, DeviceType }
import io.soheila.um.utils.UMClock
import org.specs2.Specification
import org.specs2.mock.Mockito

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class UserServiceSpec extends Specification with Mockito {
  def is = sequential ^
    s2"""
    'retrieve' with UUID should return a user when exists                                               $e1
    'retrieve' with UUID should return empty option when user doesn't exist                             $e2
    'retrieve' with loginInfo should return a user when exist                                           $e3
    'retrieve' with loginInfo should return empty option when user doesn't exist                        $e4
    'archive' with UUID should return true when use is archived                                         $e5
    'archive' with UUID should throw UserServiceException when archive fails with db error              $e6
    'save' with user entity should return the user creation is done                                     $e7
    'save' with user entity should throw UserServiceException when user creation fails with db error    $e8
    'save' with social profile for existing user updates
        the profile info and return the user when success                                               $e9
    'save' with social profile for non existing user creates and return the user when success           $e10
    'save' with social profile should throw UserServiceException user exists
        and update fails with db error                                                                  $e11
    'save' with social profile should throw UserServiceException user does not exist
        and create fails with db error                                                                  $e12
    'update' with user entity should return the user update is done                                     $e13
    'update' with user entity should throw UserServiceException when user update fails with db error    $e14
                                                        """

  implicit val system = ActorSystem()

  val userDAO = mock[UserDAO]
  val umClockMock: UMClock = mock[UMClock]
  val mockedActorRef: ActorRef = spy(TestProbe("test").ref)

  val date = LocalDateTime.now()
  umClockMock.now returns date

  val userService = new UserServiceImpl(userDAO, mockedActorRef)

  def e1 = {
    val userUuid = UUID.randomUUID().toString

    val activity = UserActivity(None, userUuid, ActivityType.Activate, "ip", DeviceType.Mobile, date)

    //    val e = MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)
    //    val res: Either[MongoDAOException, UserActivity] = Left(e)
    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.read(userUuid) returns Future.successful(Some(user))

    val response = Await.result(userService.retrieve(userUuid), 5 seconds).get
    response must be equalTo user
  }

  def e2 = {
    val userUuid = UUID.randomUUID().toString

    val activity = UserActivity(None, userUuid, ActivityType.Activate, "ip", DeviceType.Mobile, date)

    userDAO.read(userUuid) returns Future.successful(None)

    val response = Await.result(userService.retrieve(userUuid), 5 seconds)
    response must beEmpty
  }

  def e3 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.find(loginInfo) returns Future.successful(Some(user))

    val response = Await.result(userService.retrieve(loginInfo), 5 seconds).get
    response must be equalTo user
  }

  def e4 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    userDAO.find(loginInfo) returns Future.successful(None)

    val response = Await.result(userService.retrieve(loginInfo), 5 seconds)
    response must beEmpty
  }

  def e5 = {
    val userUuid = UUID.randomUUID().toString

    userDAO.archive(userUuid) returns Future.successful(Right(true))

    val response = Await.result(userService.archive(userUuid), 5 seconds)
    response must be
    true
  }

  def e6 = {
    val userUuid = UUID.randomUUID().toString

    userDAO.archive(userUuid) returns Future.successful(Left(UMDAOException("error", null)))

    Await.result(userService.archive(userUuid), 5 seconds) must throwAn[UserServiceException]
  }

  def e7 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.create(user) returns Future.successful(Right(user))

    val response = Await.result(userService.save(user), 5 seconds)
    response must be equalTo user
  }

  def e8 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.create(user) returns Future.successful(Left(MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)))

    Await.result(userService.save(user), 5 seconds) must throwAn[UserServiceException]
  }

  def e9 = {
    val uuid = UUID.randomUUID().toString
    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val user = User(uuid = Some(uuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val commonSocialProfile = CommonSocialProfile(loginInfo, firstName = Some("updatedFirstName"))

    val userCapture = capture[User]
    val userUUIDCapture = capture[String]

    userDAO.find(commonSocialProfile.loginInfo) returns Future.successful(Some(user))
    userDAO.update(userUUIDCapture, userCapture) returns Future.successful(Right(uuid))

    val response = Await.result(userService.save(commonSocialProfile), 5 seconds)

    there was one(userDAO).update(userUUIDCapture, userCapture)

    val capturedUser = userCapture.value

    capturedUser.firstName.get must be equalTo "updatedFirstName"
  }

  def e10 = {
    val uuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(uuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val commonSocialProfile = CommonSocialProfile(loginInfo = loginInfo, firstName = Some("updatedFirstName"),
      lastName = Some("lastName"), fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.create(any[User]) returns Future.successful(Right(user))
    userDAO.find(commonSocialProfile.loginInfo) returns Future.successful(None)

    val response = Await.result(userService.save(commonSocialProfile), 5 seconds)

    response.firstName.get must be equalTo "firstName"
  }

  def e11 = {
    val uuid = UUID.randomUUID().toString
    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val user = User(uuid = Some(uuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val commonSocialProfile = CommonSocialProfile(loginInfo, firstName = Some("updatedFirstName"))

    val userCapture = capture[User]
    val userUUIDCapture = capture[String]

    userDAO.find(commonSocialProfile.loginInfo) returns Future.successful(Some(user))
    userDAO.update(userUUIDCapture, userCapture) returns Future.successful(Left(MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)))

    Await.result(userService.save(commonSocialProfile), 5 seconds) must throwAn[UserServiceException]
  }

  def e12 = {
    val uuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(uuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val commonSocialProfile = CommonSocialProfile(loginInfo = loginInfo, firstName = Some("updatedFirstName"),
      lastName = Some("lastName"), fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.create(any[User]) returns Future.successful(Left(MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)))
    userDAO.find(commonSocialProfile.loginInfo) returns Future.successful(None)

    Await.result(userService.save(commonSocialProfile), 5 seconds) must throwAn[UserServiceException]
  }

  def e13 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.update(userUuid, user) returns Future.successful(Right(userUuid))

    val response = Await.result(userService.update(user), 5 seconds)
    response must be equalTo user
  }

  def e14 = {
    val userUuid = UUID.randomUUID().toString

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val user = User(uuid = Some(userUuid), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    userDAO.update(userUuid, user) returns Future.successful(Left(MongoDAOException("error", ErrorCode.GENERIC_DATABASE_ERROR, null)))

    Await.result(userService.update(user), 5 seconds) must throwAn[UserServiceException]
  }
}
