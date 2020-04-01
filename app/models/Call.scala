package models
import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Call(userId: Long, name: String, phone: String)

object Call {
  implicit val callFormat: Format[Call] = Json.format[Call]

  def save(call: Call): Call with Persisted = {
    Db.save[Call](call)
  }

  def delete(call: Call): Unit = {
    Db.delete[Call](call)
  }

  def update(id: Long, call: Call): List[Call with Persisted] = {
    Db.query[Call].whereEqual("id", id).replace(call)
  }

  def getCalls(): Stream[Call with Persisted] = {
    Db.query[Call].fetch()
  }
}