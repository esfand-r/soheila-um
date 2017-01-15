package io.soheila.um

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import io.soheila.um.entities.User

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}
