package models

import play.api.libs.json.{Format, Json}
import sorm.{Persisted, Querier}

case class Room(
                 timestamp: Long,
                 profileAvatar: String,
                 isOnline: Boolean,
                 notificationsOn: Boolean,
                 chatType: String,
                 name: String,
                 newMessagesCount: Int,
                 creatorUserId: Long,
                 recipientUserId: Long
               )

object Room {
  implicit val format: Format[Room] = Json.format[Room]

  def save(room: Room): Room with Persisted = {
    Db.save[Room](room)
  }

  def getAllRooms(): Stream[Room with Persisted] = {
    Db.query[Room].order("timestamp", reverse = true).fetch()
  }

  def getRooms(id: Long): Stream[Room with Persisted] = {
    Db.query[Room].whereEqual("id", id).order("timestamp", reverse = true).fetch()
  }

  def isOwner(id: Long): Stream[Room with Persisted] = {
    Db.query[Room].whereEqual("creatorUserId", id).order("timestamp", reverse = true).fetch()
  }

  def getRoomsByRecipientId(id: Long): Stream[Room with Persisted] = {
    Db.query[Room].where(Querier.Or(Querier.Equal("creatorUserId", id), Querier.Equal("recipientUserId", id))).order("timestamp", reverse = true).fetch()
  }

  def getRoomByRecipientId(ownerId: Long, recipientId: Long): Option[Room with Persisted] = {
    Db.query[Room]
      .where(Querier.And(Querier.Equal("creatorUserId", ownerId),
      Querier.Equal("recipientUserId", recipientId))).order("timestamp", reverse = true).fetchOne()
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
