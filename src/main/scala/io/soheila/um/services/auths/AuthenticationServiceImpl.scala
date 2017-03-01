package io.soheila.um.services.auths

import javax.inject.Inject

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{ Credentials, _ }
import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent, Silhouette }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfileBuilder, CredentialsProvider, SocialProvider, SocialProviderRegistry }
import io.soheila.um.DefaultEnv
import io.soheila.um.entities.{ AuthToken, User }
import io.soheila.um.exceptions.AuthenticationException
import io.soheila.um.services.accounts.UserService
import io.soheila.um.vos._
import io.soheila.um.vos.accounts.{ ChangePasswordVO, ResetPasswordVO, SignInVO, SignUpVO }
import io.soheila.um.vos.auths.Token
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Handles actions to authenticate.
 */
class AuthenticationServiceImpl @Inject() (
    passwordHasher: PasswordHasher,
    passwordHasherRegistry: PasswordHasherRegistry,
    val messagesApi: MessagesApi,
    silhouette: Silhouette[DefaultEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    authTokenService: AuthTokenService,
    credentialsProvider: CredentialsProvider,
    avatarService: AvatarService,
    configuration: Configuration,
    socialProviderRegistry: SocialProviderRegistry,
    clock: Clock,
    @Named("activity-manager") activityManager: ActorRef
)(implicit val executionContext: ExecutionContext) extends AuthenticationService with I18nSupport {
  val logger = play.api.Logger(this.getClass)

  override def signUp(signUpData: SignUpVO)(implicit request: RequestHeader): Future[String] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, signUpData.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) => Future.failed(AuthenticationException(Messages("user.exists"), null))
      case None =>
        val authInfo = passwordHasher.hash(signUpData.password)
        val user = User(
          uuid = None,
          loginInfo = loginInfo,
          firstName = Some(signUpData.firstName),
          lastName = Some(signUpData.lastName),
          fullName = Some(signUpData.firstName + " " + signUpData.lastName),
          email = Some(signUpData.email),
          avatarURL = None,
          activated = true
        )
        for {
          avatar <- avatarService.retrieveURL(signUpData.email)
          user <- userService.save(user.copy(avatarURL = avatar))
          authInfo <- authInfoRepository.add(loginInfo, authInfo)
          authenticator <- silhouette.env.authenticatorService.create(loginInfo)
          token <- silhouette.env.authenticatorService.init(authenticator)
        } yield {
          silhouette.env.eventBus.publish(SignUpEvent(user, request))
          silhouette.env.eventBus.publish(LoginEvent(user, request))
          token
        }
    }.recover {
      case e =>
        logger.error(e.getMessage, e)
        throw AuthenticationException(Messages("invalid.credentials"), e)
    }
  }

  override def resetPassword(authToken: AuthToken, resetPasswordData: ResetPasswordVO)(implicit request: RequestHeader): Future[Boolean] = {
    userService.retrieve(authToken.userID.toString).flatMap {
      case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
        val passwordInfo = passwordHasherRegistry.current.hash(resetPasswordData.password)
        authInfoRepository.update[PasswordInfo](user.loginInfo, passwordInfo).map { _ => true }
      case _ => Future.failed(AuthenticationException(Messages("invalid.reset.link"), null))
    }.recover {
      case e =>
        logger.error(e.getMessage, e)
        throw AuthenticationException(Messages("invalid.reset.link"), e)
    }
  }

  override def changePassword(credentials: Credentials, changePasswordData: ChangePasswordVO)(implicit request: RequestHeader): Future[Boolean] = {
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      val passwordInfo = passwordHasherRegistry.current.hash(changePasswordData.newPassword)
      authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
        true
      }
    }.recover {
      case e =>
        logger.error(e.getMessage, e)
        throw AuthenticationException(Messages("current.password.invalid"), e)
    }
  }

  override def signIn(signInData: SignInVO)(implicit request: RequestHeader): Future[Token] = {
    credentialsProvider.authenticate(Credentials(signInData.email, signInData.password)).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
          authenticator =>
            val c = configuration.underlying
            authenticator.copy(
              expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
              idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
            )
        }.flatMap { authenticator =>
          silhouette.env.eventBus.publish(LoginEvent(user, request))
          silhouette.env.authenticatorService.init(authenticator).map { token =>
            Token(token, authenticator.expirationDateTime)
          }
        }
        case None => Future.failed(AuthenticationException("Couldn't find user", new IdentityNotFoundException("Couldn't find user")))
      }
    }.recover {
      case e =>
        logger.error(e.getMessage, e)
        throw AuthenticationException(Messages("login.failed"), e)
    }
  }

  override def socialSignIn[B](provider: String)(implicit request: ExtractableRequest[B]): Future[Either[Result, String]] = {
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(Left(result))
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            Right(token)
          }
        }
      case _ => throw AuthenticationException("Could not find the provider", null)
    }).recover {
      case e =>
        logger.error(e.getMessage, e)
        throw AuthenticationException(Messages("could.not.authenticate"), e)
    }
  }
}
