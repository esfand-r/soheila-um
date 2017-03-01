package io.soheila.um.utils

import java.time.LocalDateTime

/**
 * A trait which provides a mockable implementation for a DateTime instance.
 */
trait UMClock {

  /**
   * Gets the current DateTime.
   *
   * @return the current DateTime.
   */
  def now: LocalDateTime
}

/**
 * Creates a clock implementation.
 */
object UMClock {

  /**
   * Gets a Clock implementation.
   *
   * @return A Clock implementation.
   */
  def apply() = new UMClock {
    def now = LocalDateTime.now
  }
}

