package com.soheila.um

import com.google.inject.{ AbstractModule, Provides }
import com.soheila.um.daos.{ AuthTokenDAO, AuthTokenDAOImpl }
import com.soheila.um.services.{ AuthTokenService, AuthTokenServiceImpl }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.DummyStateProvider
import com.mohiva.play.silhouette.persistence.daos.{ DelegableAuthInfoDAO, MongoAuthInfoDAO }
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import net.codingwell.scalaguice.ScalaModule
import com.soheila.um.jobs.{ AccountJobScheduler, AuthTokenCleaner }
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext.Implicits.global

class UserManagementModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure() = {
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bind[AccountJobScheduler].asEagerSingleton()
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
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
}
