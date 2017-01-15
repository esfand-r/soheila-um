package io.soheila.um

import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.crypto.{ Crypter, CrypterAuthenticatorEncoder }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.crypto.{ JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators.{ JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{ DelegableAuthInfoDAO, InMemoryAuthInfoDAO }
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import io.soheila.um.daos._
import io.soheila.um.daos.accounts.{ MongoUserDAO, UserDAO }
import io.soheila.um.daos.activities.{ MongoUserActivityDAO, UserActivityDAO }
import io.soheila.um.daos.auths.{ AuthTokenDAO, InMemoryAuthTokenDAO }
import io.soheila.um.services.accounts.{ UserService, UserServiceImpl }
import io.soheila.um.services.auths.{ AuthTokenService, AuthTokenServiceImpl, AuthenticationService, AuthenticationServiceImpl }
import io.soheila.um.services._
import io.soheila.um.services.activities.{ ActivityManager, MongoUserActivityService, UserActivityService }
import io.soheila.um.utils.UMClock
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

/**
 * A fake Guice module for testing the application.
 */
class FakeModule(implicit val ec: ExecutionContext) extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  def configure() = {
    implicit lazy val formatPasswordInfo = Json.format[PasswordInfo]
    implicit lazy val formatOAuth1Info = Json.format[OAuth1Info]
    implicit lazy val formatOAuth2Info = Json.format[OAuth2Info]

    bind[Clock].toInstance(Clock())
    bind[UMClock].toInstance(UMClock())
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[MongoUserDAO]
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[AvatarService].to[TestGravatarService]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[SocialProviderRegistry].toInstance(SocialProviderRegistry(Seq()))
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[AuthTokenDAO].to[InMemoryAuthTokenDAO]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[AuthenticationService].to[AuthenticationServiceImpl]
    bindActor[ActivityManager]("activity-manager")
    bind[UserActivityDAO].to[MongoUserActivityDAO]
    bind[UserActivityService].to[MongoUserActivityService]

    bind[DelegableAuthInfoDAO[PasswordInfo]].toInstance(new InMemoryAuthInfoDAO[PasswordInfo])
    bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
    bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])
  }

  /**
   * Provides the password hasher registry.
   *
   * @param passwordHasher The default password hasher implementation.
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    PasswordHasherRegistry(passwordHasher)
  }

  /**
   * Provides the authenticator service.
   *
   * @param crypter       The crypter implementation.
   * @param idGenerator   The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock         The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-crypter") crypter: Crypter,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock
  ): AuthenticatorService[JWTAuthenticator] = {

    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator")
    val encoder = new CrypterAuthenticatorEncoder(crypter)

    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository     The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]
  ): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService          The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus             The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    //authenticatorService: AuthenticatorService[CookieAuthenticator],
    authenticatorService: AuthenticatorService[JWTAuthenticator],
    eventBus: EventBus
  ): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }
}
