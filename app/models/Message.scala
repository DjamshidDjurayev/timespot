package models

import play.api.libs.json.{Format, Json}
import sorm.{Persisted, Querier}

case class Message(
                 timestamp: Long,
                 chatType: String,
                 content: String,
                 read: Boolean,
                 status: Int,
                 ownerId: Long,
                 recipientId: Long,
                 roomId: Long
               )

object Message {
  val STATUS_PENDING = 0
  val STATUS_DELIVERED = 1
  val STATUS_SEEN = 2

  implicit val format: Format[Message] = Json.format[Message]

  def save(message: Message): Message with Persisted = {
    Db.save[Message](message)
  }

  def getMessages(): Stream[Message with Persisted] = {
    Db.query[Message].order("timestamp", reverse = true).fetch()
  }

  def getMessagesByRecipientId(id: Long): Stream[Message with Persisted] = {
    Db.query[Message].where(Querier.Or(Querier.Equal("ownerId", id), Querier.Equal("recipientId", id))).order("timestamp", reverse = true).fetch()
  }

  def getMessagesByRoomId(id: Long): Stream[Message with Persisted] = {
    Db.query[Message].whereEqual("roomId", id).order("timestamp", reverse = true).fetch()
  }

  def getMessagesByRoomIdWithUserId(id: Long, userId: Long): Stream[Message with Persisted] = {
    Db.query[Message].whereEqual("roomId", id).whereNotEqual("ownerId", userId).order("timestamp", reverse = true).fetch()
  }

  def updateUnreadCount(id: Long, userId: Long): Unit = {
    val messages = getMessagesByRoomIdWithUserId(id, userId)
    messages.foreach((message: Message with Persisted) => {
      updateFields(message, read = true, STATUS_SEEN)
    })
  }

  def getMessagesByRoomIdAndMessageId(id: Long, messageId: Long, limit: Int): Stream[Message with Persisted] = {
    if (messageId == 0) {
      Db.query[Message].whereEqual("roomId", id).order("timestamp", reverse = true).offset(messageId.toInt).limit(limit).fetch()
    } else {
      Db.query[Message].whereEqual("roomId", id).order("timestamp", reverse = true).whereSmaller("id", messageId).limit(limit).fetch()
    }
  }

  def findById(id: Long): Option[Message with Persisted] = {
    Db.query[Message].whereEqual("id", id).fetchOne()
  }

  def getUnreadMessagesCount(roomId: Long, userId: Long): Int = {
    Db.query[Message].whereEqual("roomId", roomId).whereNotEqual("ownerId", userId).whereEqual("read", false).count()
  }

  def update(id: Long, message: Message): List[Message with Persisted] = {
    Db.query[Message].whereEqual("id", id).replace(message)
  }

  def updateFields(message: Message, read: Boolean, status: Int): Message with Persisted = {
    Db.save[Message](message.copy(
      read = read,
      status = status
    ))
  }

  def delete(message: Message): Unit = {
    Db.delete[Message](message)
  }

  def getLastMessage(ownerId: Long, recipientId: Long): Option[Message with Persisted] = {
    Db.query[Message].whereEqual("ownerId", ownerId).whereEqual("recipientId", recipientId).order("timestamp", reverse = true).fetchOne()
  }

  def getLastMessageByRoomId(roomId: Long): Option[Message with Persisted] = {
    Db.query[Message].whereEqual("roomId", roomId).order("timestamp", reverse = true).fetchOne()
  }
}

