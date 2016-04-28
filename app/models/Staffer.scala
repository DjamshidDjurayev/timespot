package models


import org.joda.time.DateTime
import play.api.libs.json.{Writes, Reads, JsPath, Json}
import sorm._
import play.api.libs.functional.syntax._

/**
 * Created by dzhuraev on 3/15/16.
 */
case class Staffer(name: String, image: String, birth: DateTime, surname: String, middle_name: String, code: String, position: String)

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
//      (JsPath \ "position").read[String]
//    )(Staffer.apply _)
//
  implicit val write: Writes[Staffer] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "image").write[String] and
      (JsPath \ "birth").write[DateTime] and
      (JsPath \ "surname").write[String] and
      (JsPath \ "middle_name").write[String] and
      (JsPath \ "code").write[String] and
      (JsPath \ "position").write[String]
    )(unlift(Staffer.unapply))



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

