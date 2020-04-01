package models
import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Review(userId: Long, name: String, comment: String, rating: Float, siteIncluded: Boolean, socialIncluded: Boolean, toDirector: Boolean)


object Review {
  implicit val reviewFormat: Format[Review] = Json.format[Review]

  def save(review: Review): Review with Persisted = {
    Db.save[Review](review)
  }

  def delete(review: Review): Unit = {
    Db.delete[Review](review)
  }

  def update(id: Long, review: Review): List[Review with Persisted] = {
    Db.query[Review].whereEqual("id", id).replace(review)
  }

  def getReviews(): Stream[Review with Persisted] = {
    Db.query[Review].fetch()
  }
}
