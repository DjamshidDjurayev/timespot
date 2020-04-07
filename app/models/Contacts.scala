package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Contacts(city: String, phone: String, address: String)

case class ContactPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Contacts {
  implicit val format: Format[Contacts] = Json.format[Contacts]

  def contactList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): ContactPage[Contacts] = {
    val offset = pageSize * page
    val contacts = Db.query[Contacts].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Admin].count()
    ContactPage(contacts, page, offset, totalRows)
  }

  def save(contact: Contacts): Contacts with Persisted = {
    Db.save[Contacts](contact)
  }

  def saveContact(city: String, phone: String, address: String): Contacts with Persisted = {
    val contact = new Contacts(city, phone, address)
    Db.save[Contacts](contact)
  }

  def getContacts(): Stream[Contacts with Persisted] = {
    Db.query[Contacts].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[Contacts with Persisted] = {
    Db.query[Contacts].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, contact: Contacts): List[Contacts with Persisted] = {
    Db.query[Contacts].whereEqual("id", id).replace(contact)
  }

  def delete(contact: Contacts): Unit = {
    Db.delete[Contacts](contact)
  }
}
