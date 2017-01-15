package com.soheila.um.utils

import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.CacheLayer
import play.api.cache.CacheApi

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * Implementation of the cache layer which uses the default Play cache plugin.
 *
 * @param cacheApi Plays cache API implementation.
 */
class UMPlayCacheLayer @Inject() (cacheApi: CacheApi) extends UMCacheLayer {

  /**
   * Save a value in cache.
   *
   * @param key The item key under which the value should be saved.
   * @param value The value to save.
   * @param expiration Expiration time in seconds (0 second means eternity).
   * @return The value saved in cache.
   */
  override def save[T](key: String, value: T, expiration: Duration = Duration.Inf): Future[T] = Future.successful {
    cacheApi.set(key, value, expiration)
    value
  }

  /**
   * Finds a value in the cache.
   *
   * @param key The key of the item to found.
   * @tparam T The type of the object to return.
   * @return The found value or None if no value could be found.
   */
  override def find[T: ClassTag](key: String): Future[Option[T]] = Future.successful(cacheApi.get[T](key))

  /**
   * Remove a value from the cache.
   *
   * @param key Item key.
   * @return An empty future to wait for removal.
   */
  override def remove(key: String) = Future.successful(cacheApi.remove(key))
}
