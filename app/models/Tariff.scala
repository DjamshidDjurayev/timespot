package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Tariff(title: String, city: String, price: Double, description: String, creation_date: Long)

case class TariffPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Tariff {
  implicit val format: Format[Tariff] = Json.format[Tariff]

  def tariffList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): TariffPage[Tariff] = {
    val offset = pageSize * page
    val contacts = Db.query[Tariff].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Tariff].count()
    TariffPage(contacts, page, offset, totalRows)
  }

  def save(tariff: Tariff): Tariff with Persisted = {
    Db.save[Tariff](tariff)
  }

  def getTariffs(): Stream[Tariff with Persisted] = {
    Db.query[Tariff].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[Tariff with Persisted] = {
    Db.query[Tariff].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, tariff: Tariff): List[Tariff with Persisted] = {
    Db.query[Tariff].whereEqual("id", id).replace(tariff)
  }

  def delete(tariff: Tariff): Unit = {
    Db.delete[Tariff](tariff)
  }
}
