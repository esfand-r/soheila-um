package io.soheila.um.utils

import io.soheila.um.events._
import io.soheila.um.types.ActivityType
import org.specs2.Specification
import org.specs2.specification.core.SpecStructure

class ActivityTypeToEventMapperSpec extends Specification {
  override def is: SpecStructure =
    s2"""
      mapper should map 'ActivityType.Create' to 'UserCreated' event $e1
      mapper should map 'ActivityType.Activate' to 'UserActivated' event $e2
      mapper should map 'ActivityType.Archive' to 'UserArchived' event $e3
      mapper should map 'ActivityType.Delete' to 'UserDeleted' event $e4
      mapper should map 'ActivityType.Login' to 'UserLoggedIn' event $e5
      mapper should map 'ActivityType.Logout' to 'UserLoggedOut' event $e6
      mapper should map 'ActivityType.PasswordChange' to 'PasswordChanged' event $e7
      mapper should map 'ActivityType.PasswordReset' to 'PasswordReset' event $e8
      mapper should map 'ActivityType.Update' to 'UserUpdated' event $e9
    """

  val uuid = "uuid"

  def e1 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Create)
    event must beAnInstanceOf[UserCreated]
  }

  def e2 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Activate)
    event must beAnInstanceOf[UserActivated]
  }

  def e3 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Archive)
    event must beAnInstanceOf[UserArchived]
  }

  def e4 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Delete)
    event must beAnInstanceOf[UserDeleted]
  }

  def e5 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Login)
    event must beAnInstanceOf[UserLoggedIn]
  }

  def e6 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Logout)
    event must beAnInstanceOf[UserLoggedOut]
  }

  def e7 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.PasswordChange)
    event must beAnInstanceOf[PasswordChanged]
  }

  def e8 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.PasswordReset)
    event must beAnInstanceOf[PasswordReset]
  }

  def e9 = {
    val event = ActivityTypeToEventMapper.mapActivity(uuid, ActivityType.Update)
    event must beAnInstanceOf[UserUpdated]
  }
}
