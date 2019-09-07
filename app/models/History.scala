package models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Json, OFormat}
import sorm.Persisted

/**
 * Created by dzhuraev on 4/20/16.
 */
case class History(staffer: Staffer, action: Int, action_date: DateTime, action_value: org.joda.time.LocalDate)

object History {
  implicit val historyFormat: OFormat[History] = Json.format[History]

  def findById(staffer: Staffer with Persisted): Stream[History with Persisted] = {
    Db.query[History].whereEqual("staffer.id", staffer.id).order("id", reverse = true).fetch()
  }

  def getStaffActionsByDate(staffer: Staffer with Persisted, dataTime: Long): Stream[History with Persisted] = {
    val bal = new LocalDate(dataTime)
    Db.query[History].whereEqual("staffer.id", staffer.id).whereEqual("action_value", bal).order("id", reverse = true).fetch()
  }

  def historyCount(staffer: Staffer with Persisted, date: Long): Int = {
    val bal = new LocalDate(date)
    Db.query[History].whereEqual("staffer.id", staffer.id).whereEqual("action_value", bal).count()
  }

  def historyGeneralCount(): Int = {
    Db.query[History].count()
  }

}
