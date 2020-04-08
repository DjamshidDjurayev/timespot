package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class MedHistory(title: String, description: String, date: Long, fileName: String, fileFormat: String, historyType: String)

case class MedHistoryPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object MedHistory {
  implicit val format: Format[MedHistory] = Json.format[MedHistory]

  def medHistoryList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): MedHistoryPage[MedHistory] = {
    val offset = pageSize * page
    val contacts = Db.query[MedHistory].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[MedHistory].count()
    MedHistoryPage(contacts, page, offset, totalRows)
  }

  def save(medHistory: MedHistory): MedHistory with Persisted = {
    Db.save[MedHistory](medHistory)
  }

  def getMedHistoryList(): Stream[MedHistory with Persisted] = {
    Db.query[MedHistory].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[MedHistory with Persisted] = {
    Db.query[MedHistory].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, medHistory: MedHistory): List[MedHistory with Persisted] = {
    Db.query[MedHistory].whereEqual("id", id).replace(medHistory)
  }

  def delete(medHistory: MedHistory): Unit = {
    Db.delete[MedHistory](medHistory)
  }

  def getMedHistoryByType(historyType: String): Stream[MedHistory with Persisted] = {
    Db.query[MedHistory].order("id", reverse = true).whereEqual("historyType", historyType).fetch()
  }
}
