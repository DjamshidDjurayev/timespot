package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Assigner(name: String, surname: String, middleName: String, phone: String, avatar: String, speciality: String, city: String)

case class AssignerPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Assigner {
  implicit val format: Format[Assigner] = Json.format[Assigner]

  def assignersList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): AssignerPage[Assigner] = {
    val offset = pageSize * page
    val contacts = Db.query[Assigner].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Assigner].count()
    AssignerPage(contacts, page, offset, totalRows)
  }

  def save(assigner: Assigner): Assigner with Persisted = {
    Db.save[Assigner](assigner)
  }

  def getAssigners(): Stream[Assigner with Persisted] = {
    Db.query[Assigner].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[Assigner with Persisted] = {
    Db.query[Assigner].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, assigner: Assigner): List[Assigner with Persisted] = {
    Db.query[Assigner].whereEqual("id", id).replace(assigner)
  }

  def delete(assigner: Assigner): Unit = {
    Db.delete[Assigner](assigner)
  }
}
