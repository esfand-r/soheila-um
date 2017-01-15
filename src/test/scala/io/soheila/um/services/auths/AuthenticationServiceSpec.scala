package io.soheila.um.services.auths

import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestProbe
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorResult, AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.typesafe.config.Config
import io.soheila.um.DefaultEnv
import io.soheila.um.entities.{ AuthToken, User }
import io.soheila.um.exceptions.AuthenticationException
import io.soheila.um.services.accounts.UserService
import io.soheila.um.utils.UMClock
import io.soheila.um.vos.accounts.{ ChangePasswordVO, ResetPasswordVO, SignInVO, SignUpVO }
import net.ceedubs.ficus.FicusConfig
import org.joda.time.DateTime
import org.specs2.Specification
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.{ ExecutionEnvironment, _ }
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.RequestHeader
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ Await, Future }
import scala.reflect.ClassTag

class AuthenticationServiceSpec extends Specification with Mockito with ExecutionEnvironment with BeforeEach {

  override def is(implicit executionEnv: ExecutionEnv) = sequential ^
    s2"""
    'signUp' method throw and exception when user already exists                            $e1
    'signUp' method should result in a token when user does not
        exist and required info are provided                                                $e2
    'resetPassword' method throw and AuthenticationException
        when no user can be found using the token                                           $e3
    'resetPassword' should make call to update password and return token if successful      $e4
    'changePassword' method throw and an AuthenticationException when authentication fails  $e5
    'changePassword' should make call to update password and return true if successful      $e6
    'signIn' method should throw and an AuthenticationException when authentication fails   $e7
    'signIn' method should return token when authentication succeeds                        $e8
    'socialSignIn' method throw and an AuthenticationException when authentication fails    $e9
    'socialSignIn' method should return result when redirection is required                 $e10
                                                        """

  override def before = {
    org.mockito.Mockito.reset(authInfoRepositoryMock)
    org.mockito.Mockito.reset(credentialsProviderMock)
  }

  implicit val system = ActorSystem()

  val passwordHasherMock: PasswordHasher = mock[PasswordHasher]
  val passwordHasherRegistry: PasswordHasherRegistry = mock[PasswordHasherRegistry]
  val messageApiMock: MessagesApi = mock[MessagesApi]
  val silhouetteMock: Silhouette[DefaultEnv] = mock[Silhouette[DefaultEnv]]
  val userServiceMock: UserService = mock[UserService]
  val authInfoRepositoryMock: AuthInfoRepository = mock[AuthInfoRepository]
  val authTokenServiceMock: AuthTokenService = mock[AuthTokenService]
  val credentialsProviderMock: CredentialsProvider = mock[CredentialsProvider]
  val avatarServiceMock: AvatarService = mock[AvatarService]
  val configurationMock: Configuration = mock[Configuration]
  val socialProviderRegistryMock: SocialProviderRegistry = mock[SocialProviderRegistry]
  val umClockMock: UMClock = mock[UMClock]
  val mockedActorRef: ActorRef = spy(TestProbe("test").ref)

  val authTokenServiceImpl: AuthenticationService =
    new AuthenticationServiceImpl(passwordHasherMock, passwordHasherRegistry, messageApiMock, silhouetteMock, userServiceMock,
      authInfoRepositoryMock, authTokenServiceMock, credentialsProviderMock, avatarServiceMock,
      configurationMock, socialProviderRegistryMock, umClockMock, mockedActorRef)

  passwordHasherRegistry.current returns passwordHasherMock

  val date = LocalDateTime.now()
  umClockMock.now returns date

  def e1 = {
    implicit val requestHeader = mock[RequestHeader]

    val signupVO = SignUpVO("firstName", "lastName", "test@test.com", "password")
    val loginInfo = LoginInfo(CredentialsProvider.ID, signupVO.email)

    val userUUID = UUID.randomUUID().toString
    val user = User(uuid = Some(userUUID), loginInfo = loginInfo, firstName = Some(signupVO.firstName), lastName = Some(signupVO.lastName),
      fullName = Some(signupVO.lastName + " " + signupVO.lastName), email = Some(signupVO.email))

    messageApiMock.apply("user.exists") returns "error"
    userServiceMock.retrieve(loginInfo) returns Future.successful(Some(user))

    Await.result(authTokenServiceImpl.signUp(signupVO), 5 seconds) must throwAn[AuthenticationException]
  }

  def e2 = {
    implicit val requestHeader = mock[RequestHeader]

    val token = "token"

    val signupVO = SignUpVO("firstName", "lastName", "test@test.com", "password")
    val loginInfo = LoginInfo(CredentialsProvider.ID, signupVO.email)

    val userUUID = UUID.randomUUID().toString
    val user = User(uuid = Some(userUUID), loginInfo = loginInfo, firstName = Some(signupVO.firstName), lastName = Some(signupVO.lastName),
      fullName = Some(signupVO.lastName + " " + signupVO.lastName), email = Some(signupVO.email))

    val mockEnv: com.mohiva.play.silhouette.api.Environment[DefaultEnv] = mock[com.mohiva.play.silhouette.api.Environment[DefaultEnv]]
    val authenticatorServiceMock = mock[AuthenticatorService[JWTAuthenticator]]
    val authenticatorMock = mock[JWTAuthenticator]
    val passwordInfoMock = mock[PasswordInfo]
    val eventBusMock = mock[EventBus]

    passwordHasherMock.hash(signupVO.password) returns passwordInfoMock
    authenticatorServiceMock.create(loginInfo) returns Future.successful(authenticatorMock)
    authenticatorServiceMock.init(authenticatorMock) returns Future.successful(token)

    authInfoRepositoryMock.add(loginInfo, passwordInfoMock) returns Future.successful(passwordInfoMock)
    mockEnv.authenticatorService returns authenticatorServiceMock
    mockEnv.eventBus returns eventBusMock

    silhouetteMock.env returns mockEnv
    eventBusMock.publish(SignUpEvent(user, requestHeader))

    userServiceMock.retrieve(loginInfo) returns Future.successful(None)
    avatarServiceMock.retrieveURL(signupVO.email) returns Future.successful(Some("url"))
    userServiceMock.save(any[User]) returns Future.successful(user.copy(avatarURL = Some("url")))

    val tokenAfterSignUp = Await.result(authTokenServiceImpl.signUp(signupVO), 5 seconds)

    tokenAfterSignUp must be equalTo (token)
  }

  def e3 = {
    implicit val requestHeader = mock[RequestHeader]

    val tokenId = UUID.randomUUID()
    val userUUID = UUID.randomUUID()
    val expiry = LocalDateTime.now()
    val authToken = AuthToken(tokenId, userUUID, expiry)
    val passwordVO = ResetPasswordVO("newPassword")

    val passwordHasherMock = mock[PasswordHasher]

    userServiceMock.retrieve(authToken.userID.toString) returns Future.successful(None)

    Await.result(authTokenServiceImpl.resetPassword(authToken, passwordVO), 5 seconds) must throwAn[AuthenticationException]
  }

  def e4 = {
    implicit val requestHeader = mock[RequestHeader]

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")

    val uuid = UUID.randomUUID()

    val user = User(uuid = Some(uuid.toString()), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val tokenId = UUID.randomUUID()
    val userUUID = UUID.randomUUID()
    val expiry = LocalDateTime.now()
    val authToken = AuthToken(tokenId, uuid, expiry)
    val passwordVO = ResetPasswordVO("newPassword")

    val passwordInfo = PasswordInfo("hasher", passwordVO.password)

    passwordHasherMock.hash(passwordVO.password) returns passwordInfo

    val loginInfoCaptor = capture[LoginInfo]
    val passwordInfoCaptor = capture[PasswordInfo]

    authInfoRepositoryMock.update[PasswordInfo](loginInfo, passwordInfo) returns Future.successful(passwordInfo)
    userServiceMock.retrieve(authToken.userID.toString) returns Future.successful(Some(user))

    val response = Await.result(authTokenServiceImpl.resetPassword(authToken, passwordVO), 5 seconds)

    (there was one(authInfoRepositoryMock).update[PasswordInfo](loginInfoCaptor, passwordInfoCaptor)) and
      (response must beTrue)
  }

  def e5 = {
    implicit val requestHeader = mock[RequestHeader]

    val tokenId = UUID.randomUUID()
    val userUUID = UUID.randomUUID()
    val expiry = LocalDateTime.now()
    val authToken = AuthToken(tokenId, userUUID, expiry)
    val passwordVO = ResetPasswordVO("newPassword")

    val passwordHasherMock = mock[PasswordHasher]
    val credentials = Credentials(CredentialsProvider.ID, "oldPassword")
    val changePasswordVO = ChangePasswordVO("oldPassword", "newPassword")

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val passwordInfo = PasswordInfo("hasher", passwordVO.password)

    credentialsProviderMock.authenticate(credentials) returns Future.successful(loginInfo)
    passwordHasherMock.hash(passwordVO.password) returns passwordInfo
    authInfoRepositoryMock.update[PasswordInfo](loginInfo, passwordInfo) throws new RuntimeException("test")

    Await.result(authTokenServiceImpl.changePassword(credentials, changePasswordVO), 5 seconds) must throwAn[AuthenticationException]
  }

  def e6 = {
    implicit val requestHeader = mock[RequestHeader]

    val credentials = Credentials(CredentialsProvider.ID, "oldPassword")
    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val changePasswordVO = ChangePasswordVO("oldPassword", "newPassword")

    val uuid = UUID.randomUUID()

    val user = User(uuid = None, loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val tokenId = UUID.randomUUID()
    val userUUID = UUID.randomUUID()
    val expiry = LocalDateTime.now()
    val authToken = AuthToken(tokenId, uuid, expiry)
    val passwordVO = ResetPasswordVO("newPassword")

    val passwordInfo = PasswordInfo("hasher", passwordVO.password)

    val loginInfoCaptor = capture[LoginInfo]
    val passwordInfoCaptor = capture[PasswordInfo]

    credentialsProviderMock.authenticate(credentials) returns Future.successful(loginInfo)
    passwordHasherMock.hash(passwordVO.password) returns passwordInfo
    authInfoRepositoryMock.update[PasswordInfo](loginInfo, passwordInfo) returns Future.successful(passwordInfo)

    val response = Await.result(authTokenServiceImpl.changePassword(credentials, changePasswordVO), 5 seconds)

    (there was one(authInfoRepositoryMock).update[PasswordInfo](loginInfoCaptor, passwordInfoCaptor)) and
      (response must beTrue)
  }

  def e7 = {
    implicit val requestHeader = mock[RequestHeader]

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val signinVO = SignInVO("test@test.com", "password")
    val credentials = Credentials(signinVO.email, signinVO.password)

    credentialsProviderMock.authenticate(credentials) returns Future.successful(loginInfo)

    userServiceMock.retrieve(loginInfo) returns Future.successful(None)

    Await.result(authTokenServiceImpl.signIn(signinVO), 5 seconds) must throwAn[AuthenticationException]
  }

  def e8 = {
    implicit val requestHeader = mock[RequestHeader]

    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val signinVO = SignInVO("test@test.com", "password")
    val credentials = Credentials(signinVO.email, signinVO.password)
    val token = "token"

    val uuid = UUID.randomUUID()

    val user = User(uuid = Some(uuid.toString()), loginInfo = loginInfo, firstName = Some("firstName"), lastName = Some("lastName"),
      fullName = Some("firstName lastName"), email = Some("test@test.com"))

    val mockEnv: com.mohiva.play.silhouette.api.Environment[DefaultEnv] = mock[com.mohiva.play.silhouette.api.Environment[DefaultEnv]]
    val authenticatorServiceMock = mock[AuthenticatorService[JWTAuthenticator]]
    val authenticatorMock = mock[JWTAuthenticator]
    val eventBusMock = mock[EventBus]
    val ficusConfigMock = mock[FicusConfig]
    val typesafeConfigMock = mock[Config]
    val authenticatorMockCaptor = capture[JWTAuthenticator]

    silhouetteMock.env returns mockEnv
    credentialsProviderMock.authenticate(credentials) returns Future.successful(loginInfo)
    ficusConfigMock.config returns typesafeConfigMock
    authenticatorMock.expirationDateTime returns DateTime.now()
    userServiceMock.retrieve(loginInfo) returns Future.successful(Some(user))
    mockEnv.authenticatorService returns authenticatorServiceMock
    authenticatorServiceMock.create(loginInfo) returns Future.successful(authenticatorMock)
    authenticatorServiceMock.init(org.mockito.Matchers.anyObject[JWTAuthenticator])(any[RequestHeader]) returns Future.successful(token)
    mockEnv.eventBus returns eventBusMock
    eventBusMock.publish(LoginEvent(user, requestHeader))
    ficusConfigMock.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry") returns FiniteDuration(10, TimeUnit.SECONDS)
    ficusConfigMock.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout") returns Some(FiniteDuration(10, TimeUnit.SECONDS))
    configurationMock.underlying returns ficusConfigMock.config

    val tokenAfterSignIn = Await.result(authTokenServiceImpl.signIn(signinVO), 5 seconds)

    tokenAfterSignIn must not beNull
  }

  def e9 = {
    implicit val requestHeader = mock[ExtractableRequest[String]]

    val provider = "google"
    val oauth2ProviderMock = mock[OAuth2Provider]

    socialProviderRegistryMock.get[SocialProvider](provider) returns Some(oauth2ProviderMock)
    oauth2ProviderMock.authenticate() throws new RuntimeException("test")

    Await.result(authTokenServiceImpl.socialSignIn(provider), 5 seconds) must throwAn[AuthenticationException]
  }

  def e10 = {
    implicit val requestHeader = mock[ExtractableRequest[String]]

    val provider = "google"
    val oauth2ProviderMock = mock[GoogleProvider]
    val authenticationResultMock = mock[AuthenticatorResult]

    socialProviderRegistryMock.get[SocialProvider](provider) returns Some(oauth2ProviderMock)
    oauth2ProviderMock.authenticate() returns Future.successful(Left(authenticationResultMock))

    val response = Await.result(authTokenServiceImpl.socialSignIn(provider), 5 seconds).left.get
    response must be equalTo authenticationResultMock
  }

  def e11 = {
    implicit val requestHeader = mock[ExtractableRequest[String]]

    val token = "token"
    val provider = "google"
    val oauth2ProviderMock = mock[GoogleProvider]
    val oAuth2InfoMock = mock[OAuth2Info]
    val profile = mock[oauth2ProviderMock.Profile]
    val mockUser = mock[User]
    val loginInfo = LoginInfo(CredentialsProvider.ID, "test@test.com")
    val authenticatorServiceMock = mock[AuthenticatorService[JWTAuthenticator]]
    val mockEnv: com.mohiva.play.silhouette.api.Environment[DefaultEnv] = mock[com.mohiva.play.silhouette.api.Environment[DefaultEnv]]
    val authenticatorMock = mock[JWTAuthenticator]
    val eventBusMock = mock[EventBus]

    profile.loginInfo returns loginInfo
    socialProviderRegistryMock.get[SocialProvider](provider) returns Some(oauth2ProviderMock)
    oauth2ProviderMock.authenticate() returns Future.successful(Right(oAuth2InfoMock))
    oauth2ProviderMock.retrieveProfile(oAuth2InfoMock) returns Future.successful(profile)
    userServiceMock.save(profile) returns Future.successful(mockUser)
    authInfoRepositoryMock.save(profile.loginInfo, oAuth2InfoMock) returns Future.successful(oAuth2InfoMock)
    mockEnv.authenticatorService returns authenticatorServiceMock
    authenticatorServiceMock.create(profile.loginInfo) returns Future.successful(authenticatorMock)
    authenticatorServiceMock.init(authenticatorMock) returns Future.successful(token)
    mockEnv.eventBus returns eventBusMock
    eventBusMock.publish(LoginEvent(mockUser, requestHeader))

    val response = Await.result(authTokenServiceImpl.socialSignIn(provider), 5 seconds).right.get
    response must be equalTo token
  }
}
