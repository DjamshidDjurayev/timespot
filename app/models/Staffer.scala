package models


import org.joda.time.DateTime
import play.api.libs.json.{Writes, Reads, JsPath, Json}
import sorm._
import play.api.libs.functional.syntax._

/**
 * Created by dzhuraev on 3/15/16.
 */
case class Staffer(name: String, image: String, birth: DateTime, surname: String, middle_name: String, code: String, position: String, email: String)

case class Page[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Staffer {
  implicit val format = Json.format[Staffer]
//  implicit val read: Reads[Staffer] = Json.reads[Staffer]
//  implicit val write: Writes[Staffer] = Json.writes[Staffer]

//  implicit val personReads = (
//    (JsPath \ 'name).read[String] and
//      (JsPath \ 'image).read[String] and
//      (JsPath \ 'birth).read[DateTime] and
//      (JsPath \ 'surname).read[String] and
//      (JsPath \ 'middle_name).read[String] and
//      (JsPath \ 'code).read[String] and
//      (JsPath \ 'position).read[String]
//    )(Staffer.apply _)

//  implicit val read: Reads[Staffer] = (
//    (JsPath \ "name").read[String] and
//      (JsPath \ "image").read[String] and
//      (JsPath \ "birth").read[DateTime] and
//      (JsPath \ "surname").read[String] and
//      (JsPath \ "middle_name").read[String] and
//      (JsPath \ "code").read[String] and
//      (JsPath \ "position").read[String] and
//      (JsPath \ "email").read[String]
//    )(Staffer.apply _)
////
//  implicit val write: Writes[Staffer] = (
//    (JsPath \ "name").write[String] and
//      (JsPath \ "image").write[String] and
//      (JsPath \ "birth").write[DateTime] and
//      (JsPath \ "surname").write[String] and
//      (JsPath \ "middle_name").write[String] and
//      (JsPath \ "code").write[String] and
//      (JsPath \ "position").write[String] and
//      (JsPath \ "email").write[String]
//    )(unlift(Staffer.unapply))



  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[Staffer] = {

    val offest = pageSize * page

    val users = Db.query[Staffer].limit(pageSize).order("id", true).offset(offest).fetch()

    val totalRows = Db.query[Staffer].count()

    Page(users, page, offest, totalRows)

  }

  def update(id: Long, staffer: Staffer) = {
    Db.query[Staffer].whereEqual("id", id).replace(staffer)
  }

  def findById(id: Long) = {
    Db.query[Staffer].whereEqual("id", id).fetchOne()
  }


  def findByQrCode(code: String) = {
    Db.query[Staffer].whereEqual("code", code).fetchOne()
  }

  def delete(id: Long) = {
    Db.delete[Staffer](Db.fetchById[Staffer](id))
  }

  def deleteByCode(staffer: Staffer) = {
    Db.delete[Staffer](staffer)
  }

}

