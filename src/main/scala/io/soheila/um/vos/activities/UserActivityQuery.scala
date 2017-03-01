package io.soheila.um.vos.activities

import java.time.LocalDateTime

import io.soheila.um.types.ActivityType.ActivityType
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Writes }

case class UserActivityQuery(
  earlierThan: Option[LocalDateTime] = None,
  laterThan: Option[LocalDateTime] = None,
  activityType: Option[ActivityType] = None,
  userUUID: Option[String] = None,
  sortFilter: Option[(String, Int)] = None
)

object UserActivityQuery {
  implicit val residentWrites: Writes[UserActivityQuery] = (
    (JsPath \ "timestamp" \ "$lte").writeNullable[LocalDateTime] and
    (JsPath \ "timestamp" \ "$gte").writeNullable[LocalDateTime] and
    (JsPath \ "activityType").writeNullable[ActivityType] and
    (JsPath \ "userUUID").writeNullable[String]
  )(ucq => (ucq.earlierThan, ucq.laterThan, ucq.activityType, ucq.userUUID))
}