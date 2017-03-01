package io.soheila.um.vos.activities

import java.time.LocalDateTime

import io.soheila.um.types.ActivityType
import org.specs2.Specification
import play.api.libs.json.Json

class UserActivityQuerySpec extends Specification {
  def is = s2"""
    This is a specification for testing UserActivityQuery conversion to json
    'Json.toJson' should return empty json when non of the fields are provided                                      $e1
    'Json.toJson' should query for finding dates greater than the provided date in query                            $e2
    'Json.toJson' should query for finding dates earlier than the provided date in query                            $e3
    'Json.toJson' should query for dates in between the provided dates in the query object                          $e4
    'Json.toJson' should query for all fields with implicit 'And' when all fields are the query are provided        $e5
                                                        """

  def e1 = {
    val userActivityQuery = UserActivityQuery()
    val query = Json.toJson(userActivityQuery)
    query.toString() must beEqualTo("{}")
  }

  def e2 = {
    val dateString = "2017-03-02T11:58:51.386"
    val date = LocalDateTime.parse(dateString)
    val userActivityQuery = UserActivityQuery(laterThan = Some(date))
    val query = Json.toJson(userActivityQuery)
    val expectedAnswer = "{\"timestamp\":{\"$gte\":\"2017-03-02T11:58:51.386\"}}"

    query.toString() must beEqualTo(expectedAnswer)
  }

  def e3 = {
    val dateString = "2017-03-02T11:58:51.386"
    val date = LocalDateTime.parse(dateString)
    val userActivityQuery = UserActivityQuery(earlierThan = Some(date))
    val query = Json.toJson(userActivityQuery)
    val expectedAnswer = "{\"timestamp\":{\"$lte\":\"2017-03-02T11:58:51.386\"}}"

    query.toString() must beEqualTo(expectedAnswer)
  }

  def e4 = {
    val date1String = "2017-03-02T11:58:51.386"
    val date1 = LocalDateTime.parse(date1String)

    val date2String = "2017-03-02T11:58:51.386"
    val date2 = LocalDateTime.parse(date2String)

    val userActivityQuery = UserActivityQuery(laterThan = Some(date1), earlierThan = Some(date2))
    val query = Json.toJson(userActivityQuery)
    val expectedAnswer = "{\"timestamp\":{\"$lte\":\"2017-03-02T11:58:51.386\",\"$gte\":\"2017-03-02T11:58:51.386\"}}"

    query.toString() must beEqualTo(expectedAnswer)
  }

  def e5 = {
    val uuid = "b5419bb3-70b0-4a0d-8042-5b0846a1c096"

    val date1String = "2017-03-02T11:58:51.386"
    val date1 = LocalDateTime.parse(date1String)

    val date2String = "2017-03-02T11:58:51.386"
    val date2 = LocalDateTime.parse(date2String)

    val userActivityQuery = UserActivityQuery(laterThan = Some(date1), earlierThan = Some(date2),
      activityType = Some(ActivityType.Logout), userUUID = Some(uuid))

    val query = Json.toJson(userActivityQuery)

    val expectedAnswer = "{\"timestamp\":{\"$lte\":\"2017-03-02T11:58:51.386\",\"$gte\":\"2017-03-02T11:58:51.386\"},\"activityType\":\"Logout\",\"userUUID\":\"b5419bb3-70b0-4a0d-8042-5b0846a1c096\"}"

    query.toString() must beEqualTo(expectedAnswer)
  }
}
