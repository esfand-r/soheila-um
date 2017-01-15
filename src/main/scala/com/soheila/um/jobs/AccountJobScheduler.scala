package com.soheila.um.jobs

import akka.actor.{ ActorRef, ActorSystem }
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
 * Schedules the jobs.
 */
class AccountJobScheduler @Inject() (
    system: ActorSystem,
    @Named("auth-token-cleaner") authTokenCleaner: ActorRef
) {

  QuartzSchedulerExtension(system).schedule("AuthTokenCleaner", authTokenCleaner, AuthTokenCleaner.Clean)

  authTokenCleaner ! AuthTokenCleaner.Clean
}
