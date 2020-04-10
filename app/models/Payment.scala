package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Payment(date: Long, paymentMethod: String, amount: Double, currency: String)

case class PaymentPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Payment {
  implicit val format: Format[Payment] = Json.format[Payment]

  def paymentList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): PaymentPage[Payment] = {
    val offset = pageSize * page
    val users = Db.query[Payment].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Payment].count()
    PaymentPage(users, page, offset, totalRows)
  }

  def save(payment: Payment): Payment with Persisted = {
    Db.save[Payment](payment)
  }

  def getPayments(): Stream[Payment with Persisted] = {
    Db.query[Payment].order("date", reverse = true).fetch()
  }

  def findById(id: Long): Option[Payment with Persisted] = {
    Db.query[Payment].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, payment: Payment): List[Payment with Persisted] = {
    Db.query[Payment].whereEqual("id", id).replace(payment)
  }

  def delete(payment: Payment): Unit = {
    Db.delete[Payment](payment)
  }
}
