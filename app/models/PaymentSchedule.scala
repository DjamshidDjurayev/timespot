package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json, Writes}
import sorm.Persisted

case class PaymentSchedule(title: String, date: Long, amount: Double, currency: String, actionType: String)

case class PaymentSchedulePage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object PaymentSchedule {
  implicit val locationWrites: Writes[PaymentSchedule with Persisted] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "title").write[String] and
      (JsPath \ "date").write[Long] and
      (JsPath \ "amount").write[Double] and
      (JsPath \ "currency").write[String] and
      (JsPath \ "action").write[String]
    )(unlift((schedule: PaymentSchedule with Persisted) => Some(schedule.id, schedule.title, schedule.date, schedule.amount, schedule.currency, schedule.actionType)))

  def scheduleList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): PaymentSchedulePage[PaymentSchedule] = {
    val offset = pageSize * page
    val contacts = Db.query[PaymentSchedule].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[PaymentSchedule].count()
    PaymentSchedulePage(contacts, page, offset, totalRows)
  }

  def save(schedule: PaymentSchedule): PaymentSchedule with Persisted = {
    Db.save[PaymentSchedule](schedule)
  }

  def getPaymentSchedules: Stream[PaymentSchedule with Persisted] = {
    Db.query[PaymentSchedule].order("date", reverse = true).fetch()
  }

  def findById(id: Long): Option[PaymentSchedule with Persisted] = {
    Db.query[PaymentSchedule].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, schedule: PaymentSchedule): List[PaymentSchedule with Persisted] = {
    Db.query[PaymentSchedule].whereEqual("id", id).replace(schedule)
  }

  def delete(schedule: PaymentSchedule): Unit = {
    Db.delete[PaymentSchedule](schedule)
  }
}
