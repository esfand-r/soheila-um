package io.soheila.um.jobs

import java.time.ZoneId
import javax.inject.Inject

import akka.actor._
import io.soheila.um.jobs.AuthTokenCleaner.Clean
import io.soheila.um.services.auths.AuthTokenService
import io.soheila.um.utils.UMClock

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A job which cleanup invalid auth tokens.
 *
 * @param service The auth token service implementation.
 * @param clock The clock implementation.
 */
class AuthTokenCleaner @Inject() (
    service: AuthTokenService,
    clock: UMClock
) extends Actor {

  val logger = play.api.Logger(this.getClass)

  /**
   * Process the received messages.
   */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
      val msg = new StringBuffer("\n")
      msg.append("=================================\n")
      msg.append("Start to cleanup auth tokens\n")
      msg.append("=================================\n")
      service.clean.map { deleted =>
        val now = clock.now.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        val seconds = (now - start) / 1000
        msg.append("Total of %s auth tokens(s) were deleted in %s seconds".format(deleted.length, seconds)).append("\n")
        msg.append("=================================\n")

        msg.append("=================================\n")
        logger.info(msg.toString)
      }.recover {
        case e =>
          msg.append("Couldn't cleanup auth tokens because of unexpected error\n")
          msg.append("=================================\n")
          logger.error(msg.toString, e)
      }
  }
}

object AuthTokenCleaner {
  case object Clean
}
