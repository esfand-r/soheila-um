package io.soheila.um.utils

import java.time.LocalDateTime

import org.specs2.Specification

class UMClockSpec extends Specification {
  override def is = sequential ^
    s2"""
    'now' should return a date                                               $e1
                                                        """

  def e1 = {
    val clock = UMClock()
    val aDate = clock.now

    aDate must beAnInstanceOf[LocalDateTime]
  }
}
