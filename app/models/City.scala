package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, Json, Writes}
import sorm.Persisted

case class City(title: String, creation_date: Long)

case class CityPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object City {
  implicit val cityWrites: Writes[City with Persisted] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "title").write[String] and
      (JsPath \ "creation_date").write[Long]
    )(unlift((city: City with Persisted) => Some(city.id, city.title, city.creation_date)))

  def cityList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): CityPage[City] = {
    val offset = pageSize * page
    val contacts = Db.query[City].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[City].count()
    CityPage(contacts, page, offset, totalRows)
  }

  def save(city: City): City with Persisted = {
    Db.save[City](city)
  }

  def getCities: Stream[City with Persisted] = {
    Db.query[City].order("title", reverse = false).fetch()
  }

  def findById(id: Long): Option[City with Persisted] = {
    Db.query[City].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, city: City): List[City with Persisted] = {
    Db.query[City].whereEqual("id", id).replace(city)
  }

  def delete(city: City): Unit = {
    Db.delete[City](city)
  }
}
