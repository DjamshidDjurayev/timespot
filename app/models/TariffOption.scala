package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class TariffOption(keyword: String, title: String)

case class TariffOptionPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object TariffOption {
  implicit val format: Format[TariffOption] = Json.format[TariffOption]

  def tariffOptionsList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): TariffOptionPage[TariffOption] = {
    val offset = pageSize * page
    val contacts = Db.query[TariffOption].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[TariffOption].count()
    TariffOptionPage(contacts, page, offset, totalRows)
  }

  def save(tariffOption: TariffOption): TariffOption with Persisted = {
    Db.save[TariffOption](tariffOption)
  }

  def getTariffOptions(): Stream[TariffOption with Persisted] = {
    Db.query[TariffOption].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[TariffOption with Persisted] = {
    Db.query[TariffOption].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, tariffOption: TariffOption): List[TariffOption with Persisted] = {
    Db.query[TariffOption].whereEqual("id", id).replace(tariffOption)
  }

  def delete(tariffOption: TariffOption): Unit = {
    Db.delete[TariffOption](tariffOption)
  }
}
