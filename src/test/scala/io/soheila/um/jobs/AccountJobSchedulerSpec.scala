package io.soheila.um.jobs

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestProbe
import org.specs2.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.core.SpecStructure
import scala.concurrent.duration._

class AccountJobSchedulerSpec extends Specification with Mockito {
  override def is: SpecStructure =
    s2"""
      Schedular should schedule the AuthToken cleaner and start it with a Clean message $e1
    """

  implicit val system = ActorSystem()

  val probe = TestProbe("test")
  val mockedActorRef: ActorRef = probe.ref

  def e1 = {
    val accountJobSchedularMock = new AccountJobScheduler(system, mockedActorRef)

    val message = probe.expectMsg(500 millis, AuthTokenCleaner.Clean)
    message must not beNull
  }
}
