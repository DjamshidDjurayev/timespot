package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Message(
                 timestamp: Long,
                 profileAvatar: String,
                 isOnline: Boolean,
                 notificationsOn: Boolean,
                 chatType: String,
                 name: String,
                 newMessagesCount: Int,
                 userId: Long
               )

object Message {
  implicit val format: Format[Message] = Json.format[Message]

  def save(message: Message): Message with Persisted = {
    Db.save[Message](message)
  }

  def getMessages(): Stream[Message with Persisted] = {
    Db.query[Message].order("timestamp", reverse = true).fetch()
  }

  def findById(id: Long): Option[Message with Persisted] = {
    Db.query[Message].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, message: Message): List[Message with Persisted] = {
    Db.query[Message].whereEqual("id", id).replace(message)
  }

  def delete(message: Message): Unit = {
    Db.delete[Message](message)
  }
}

