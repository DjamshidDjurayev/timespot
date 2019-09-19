package models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Json, OFormat, Reads, Writes}
import sorm.Persisted

import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

/**
 * Created by dzhuraev on 4/20/16.
 */
case class History(staffer: Staffer, action: Int, action_date: Long, action_value: Long)

object History {
  implicit val dateTimeWriter: Writes[DateTime] = jodaDateWrites("dd/MM/yyyy HH:mm:ss")
  implicit val dateTimeJsReader: Reads[DateTime] = jodaDateReads("yyyyMMddHHmmss")

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
