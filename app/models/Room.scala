package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Room(
                 timestamp: Long,
                 profileAvatar: String,
                 isOnline: Boolean,
                 notificationsOn: Boolean,
                 chatType: String,
                 name: String,
                 newMessagesCount: Int,
                 userId: Long
               )

object Room {
  implicit val format: Format[Room] = Json.format[Room]

  def save(room: Room): Room with Persisted = {
    Db.save[Room](room)
  }

  def getRooms(): Stream[Room with Persisted] = {
    Db.query[Room].order("timestamp", reverse = true).fetch()
  }

  def findById(id: Long): Option[Room with Persisted] = {
    Db.query[Room].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, room: Room): List[Room with Persisted] = {
    Db.query[Room].whereEqual("id", id).replace(room)
  }

  def delete(room: Room): Unit = {
    Db.delete[Room](room)
  }
}
