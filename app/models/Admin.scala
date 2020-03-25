package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

/**
 * Created by dzhuraev on 4/28/16.
 */
case class Admin(name: String, surname: String, login: String, password: String)

case class Page3[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Admin {

  implicit val adminFormat: Format[Admin] = Json.format[Admin]

  def list3(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page3[Admin] = {
    val offset = pageSize * page
    val users = Db.query[Admin].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Admin].count()
    Page3(users, page, offset, totalRows)
  }

  def findAdmin(login: String, password: String): Option[Admin with Persisted] = {
    Db.query[Admin with Persisted].whereEqual("login", login).whereEqual("password", password).fetchOne()
  }

  def findAdminByLogin(login: String): Option[Admin with Persisted] = {
    Db.query[Admin with Persisted].whereEqual("login", login).fetchOne()
  }

  def findById(id: Long): Option[Admin with Persisted] = {
    Db.query[Admin].whereEqual("id", id).fetchOne()
  }

  def getAdminId(login: String, password: String): Option[Long] = {
    Db.query[Admin].whereEqual("login", login).whereEqual("password", password).fetchOneId()
  }

  def getAdmins(): Stream[Admin with Persisted] = {
    Db.query[Admin].fetch()
  }

  def save(admin: Admin): Admin with Persisted = {
    Db.save[Admin](admin)
  }

  def delete(admin: Admin): Unit = {
    Db.delete[Admin](admin)
  }

  def update(id: Long, admin: Admin): List[Admin with Persisted] = {
    Db.query[Admin].whereEqual("id", id).replace(admin)
  }

  def adminCount(): Int = {
    Db.query[Admin].count()
  }
}