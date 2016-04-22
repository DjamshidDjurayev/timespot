package models


import java.util.Date

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.Action
import sorm._
import joda._
import play.api.data.format.Formats._

/**
 * Created by dzhuraev on 3/15/16.
 */
case class Staffer(name: String, surname: String, middle_name: String, position: String, image: String, birth: DateTime, code: String)

case class Page[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Staffer {
  implicit val staffFormat = Json.format[Staffer]



  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[Staffer] = {

    val offest = pageSize * page

    val users = DBase.query[Staffer].limit(pageSize).order("id", true).offset(offest).fetch()

    val totalRows = DBase.query[Staffer].count()

    Page(users, page, offest, totalRows)

  }

  def update(id: Long, staffer: Staffer) = {
    DBase.query[Staffer].whereEqual("id", id).replace(staffer)
  }

  def findById(id: Long) = {
    DBase.query[Staffer].whereEqual("id", id).fetchOne()
  }

  def findByQrCode(code: String) = {
    DBase.query[Staffer].whereEqual("code", code).fetchOne()
  }

  def delete(id: Long) = {
    DBase.delete[Staffer](DBase.fetchById[Staffer](id))
  }

}

