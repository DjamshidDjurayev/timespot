package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Alert(title: String, description: String, date: Long, notificationType: String, action: String)

case class Page4[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Alert {
  implicit val format: Format[Alert] = Json.format[Alert]

  def list4(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page4[Alert] = {
    val offset = pageSize * page
    val users = Db.query[Alert].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Admin].count()
    Page4(users, page, offset, totalRows)
  }

  def getAlerts(): Stream[Alert with Persisted] = {
    Db.query[Alert].fetch()
  }

  def saveAlert(title: String, description: String, date: Long, notificationType: String, action: String): Alert with Persisted = {
    val alert = new Alert(title, description, date, notificationType, action)
    Db.save[Alert](alert)
  }

  def save(alert: Alert): Alert with Persisted = {
    Db.save[Alert](alert)
  }

  def removeAlert(alert: Alert): Unit = {
    Db.delete[Alert](alert)
  }

  def updateAlert(alert: Alert): Alert with Persisted = {
    Db.save[Alert](alert)
  }

  def findById(id: Long): Option[Alert with Persisted] = {
    Db.query[Alert].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, alert: Alert): List[Alert with Persisted] = {
    Db.query[Alert].whereEqual("id", id).replace(alert)
  }

  def delete(alert: Alert): Unit = {
    Db.delete[Alert](alert)
  }

  def getNotifications(): Stream[Alert with Persisted] = {
    Db.query[Alert].fetch()
  }
}


