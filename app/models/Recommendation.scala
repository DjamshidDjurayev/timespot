package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Recommendation(title: String, startDate: Long, endDate: Long, creationDate: Long, status: String, profType: String)

case class RecommendationPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Recommendation {
  implicit val format: Format[Recommendation] = Json.format[Recommendation]

  def recommendationsList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): RecommendationPage[Recommendation] = {
    val offset = pageSize * page
    val contacts = Db.query[Recommendation].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Recommendation].count()
    RecommendationPage(contacts, page, offset, totalRows)
  }

  def save(recommendation: Recommendation): Recommendation with Persisted = {
    Db.save[Recommendation](recommendation)
  }

  def getRecommendations(): Stream[Recommendation with Persisted] = {
    Db.query[Recommendation].order("id", reverse = true).fetch()
  }

  def getRecommendationsByProf(prof: String): Stream[Recommendation with Persisted] = {
    Db.query[Recommendation].order("id", reverse = true).whereEqual("profType", prof).fetch()
  }

  def findById(id: Long): Option[Recommendation with Persisted] = {
    Db.query[Recommendation].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, recommendation: Recommendation): List[Recommendation with Persisted] = {
    Db.query[Recommendation].whereEqual("id", id).replace(recommendation)
  }

  def delete(recommendation: Recommendation): Unit = {
    Db.delete[Recommendation](recommendation)
  }
}
