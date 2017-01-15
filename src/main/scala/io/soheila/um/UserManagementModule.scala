package io.soheila.um

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.crypto.{ CookieSigner, Crypter, CrypterAuthenticatorEncoder }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{ AuthenticatorService, AvatarService }
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{ JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings }
import com.mohiva.play.silhouette.impl.authenticators.{ JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.oauth2.state.DummyStateProvider
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.persistence.daos.{ DelegableAuthInfoDAO, MongoAuthInfoDAO }
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import io.soheila.um.daos.accounts.{ MongoUserDAO, UserDAO }
import io.soheila.um.daos.activities.{ MongoUserActivityDAO, UserActivityDAO }
import io.soheila.um.daos.auths.{ AuthTokenDAO, InMemoryAuthTokenDAO }
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import net.codingwell.scalaguice.ScalaModule
import io.soheila.um.jobs.{ AccountJobScheduler, AuthTokenCleaner }
import io.soheila.um.services.accounts.UserService
import io.soheila.um.services.activities.{ ActivityManager, MongoUserActivityService, UserActivityService }
import io.soheila.um.services.auths.{ AuthTokenService, AuthTokenServiceImpl }
import io.soheila.um.utils.UMClock
import play.api.libs.concurrent.AkkaGuiceSupport
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global

class UserManagementModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure() = {
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bind[UMClock].toInstance(UMClock())
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bindActor[ActivityManager]("activity-manager")
    bind[AccountJobScheduler].asEagerSingleton()
    bind[UserDAO].to[MongoUserDAO]
    bind[UserActivityDAO].to[MongoUserActivityDAO]
    bind[UserActivityService].to[MongoUserActivityService]
    bind[AuthTokenDAO].to[InMemoryAuthTokenDAO]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[OAuth2StateProvider].to[DummyStateProvider]
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
  }

  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth1InfoDAO   The implementation of the delegable OAuth1 auth info DAO.
   * @param oauth2InfoDAO   The implementation of the delegable OAuth2 auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]
  ): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the implementation of the delegable Password auth info DAO.
   *
   * @param reactiveMongoApi The ReactiveMongo API db.
   * @param config The Play configuration.
   * @return The implementation of the delegable OPassword auth info DAO.
   */
  @Provides
  def providePasswordInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[PasswordInfo] = {
    implicit lazy val format = Json.format[PasswordInfo]
    new MongoAuthInfoDAO[PasswordInfo](reactiveMongoApi, config)
  }

  /**
   * Provides the implementation of the delegable OAuth1 auth info DAO.
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config The Play configuration.
   * @return The implementation of the delegable OAuth1 auth info DAO.
   */
  @Provides
  def provideOAuth1InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth1Info] = {
    implicit lazy val format = Json.format[OAuth1Info]
    new MongoAuthInfoDAO[OAuth1Info](reactiveMongoApi, config)
  }

  /**
   * Provides the implementation of the delegable OAuth2 auth info DAO.
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config The Play configuration.
   * @return The implementation of the delegable OAuth2 auth info DAO.
   */
  @Provides
  def provideOAuth2InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth2Info] = {
    implicit lazy val format = Json.format[OAuth2Info]
    new MongoAuthInfoDAO[OAuth2Info](reactiveMongoApi, config)
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository The auth info repository implementation.
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

  /**
   * Provides the password hasher registry.
   *
   * @param passwordHasher The default password hasher implementation.
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    new PasswordHasherRegistry(passwordHasher)
  }

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @param cookieSigner The cookie signer implementation.
   * @param crypter The crypter implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The OAuth1 token secret provider implementation.
   */
  @Provides
  def provideOAuth1TokenSecretProvider(
    @Named("oauth1-token-secret-cookie-signer") cookieSigner: CookieSigner,
    @Named("oauth1-token-secret-crypter") crypter: Crypter,
    configuration: Configuration,
    clock: Clock
  ): OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, cookieSigner, crypter, clock)
  }

  /**
   * Provides the cookie signer for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The cookie signer for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-cookie-signer")
  def provideOAuth1TokenSecretCookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying.as[JcaCookieSignerSettings]("silhouette.oauth1TokenSecretProvider.cookie.signer")

    new JcaCookieSigner(config)
  }

  /**
   * Provides the crypter for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the authenticator service.
   *
   * @param crypter The crypter implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
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
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
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
}
