package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted
import utils.JwtUtility

/**
 * Created by dzhuraev on 4/28/16.
 */
case class Admin(name: String, surname: String, login: String, password: String, token: String, middleName: String, phone: String, passport: String)

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

  def findAdminByToken(token: String): Option[Admin with Persisted] = {
    Db.query[Admin with Persisted].whereEqual("token", token).fetchOne()
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

  def updateToken(admin: Admin with Persisted): Option[Admin with Persisted] = {
    val newAdmin = Admin(admin.name, admin.surname, admin.login, admin.password, JwtUtility.createToken(admin.login + admin.password + System.currentTimeMillis()), admin.middleName, admin.phone, admin.passport)
    update(admin.id, newAdmin)
    findById(admin.id)
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

  def updateAdmin(admin: Admin with Persisted, name: String, surname: String,
                  login: String, middleName: String, phone: String, passport: String): Admin with Persisted = {
    Db.save[Admin](admin.copy(name = name, surname = surname, login = login, token = JwtUtility.createToken(login + admin.password + System.currentTimeMillis()),
      middleName = middleName, phone = phone, passport = passport))
  }

  def adminCount(): Int = {
    Db.query[Admin].count()
  }
}