package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

/**
 * Created by dzhuraev on 4/28/16.
 */
case class Admin(name: String, surname: String, login: String, password: String)

object Admin {

  implicit val adminFormat: Format[Admin] = Json.format[Admin]

  def findAdmin(login: String, password: String): Option[Admin with Persisted] = {
    Db.query[Admin with Persisted].whereEqual("login", login).whereEqual("password", password).fetchOne()
  }

  def findAdminByLogin(login: String): Option[Admin with Persisted] = {
    Db.query[Admin with Persisted].whereEqual("login", login).fetchOne()
  }

  def getAdminId(login: String, password: String): Option[Long] = {
    Db.query[Admin].whereEqual("login", login).whereEqual("password", password).fetchOneId()
  }

  def save(admin: Admin): Admin with Persisted = {
    Db.save[Admin](admin)
  }

  def delete(admin: Admin): Unit = {
    Db.delete[Admin](admin)
  }

  def adminCount(): Int = {
    Db.query[Admin].count()
  }
}