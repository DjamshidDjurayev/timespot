package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted
/**
 * Created by dzhuraev on 3/16/16.
 */

case class PaperNew(title: String, description: String, creation_date: Long, image: String)

case class Page2[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object PaperNew {
  implicit val newsFormat: Format[PaperNew] = Json.format[PaperNew]

  def list2(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page2[PaperNew] = {
    val offest = pageSize * page
    val news = Db.query[PaperNew].order("id", reverse = true).limit(pageSize).offset(offest).fetch()
    val totalRows = Db.query[PaperNew].count()
    Page2(news, page, offest, totalRows)
  }

  def getFeedList(reverse: Boolean): Stream[PaperNew with Persisted] = {
    Db.query[PaperNew].order("id", reverse).fetch()
  }

  def getFeedListWithOffset(reverse: Boolean, offset: Int, limit: Int): Stream[PaperNew with Persisted] = {
    Db.query[PaperNew].order("id", reverse).limit(limit).offset(offset).fetch()
  }

  def update(id: Long, paperNew: PaperNew): List[PaperNew with Persisted] = {
    Db.query[PaperNew].whereEqual("id", id).replace(paperNew)
  }

  def findById(id: Long): Option[PaperNew with Persisted] = {
    Db.query[PaperNew].whereEqual("id", id).fetchOne()
  }

  def delete(id: Long): Unit = {
    Db.delete[PaperNew](Db.fetchById[PaperNew](id))
  }

  def delete(paperNew: PaperNew): Unit = {
    Db.delete[PaperNew](paperNew)
  }

  def save(feed: PaperNew): PaperNew with Persisted = {
    Db.save[PaperNew](feed)
  }

  def newsCount(): Int = {
    Db.query[PaperNew].count()
  }

  def clearNews(): Unit = {
    val newsList = getFeedList(false)
    for (news <- newsList) {
      Db.delete[PaperNew](news)
    }
  }

}