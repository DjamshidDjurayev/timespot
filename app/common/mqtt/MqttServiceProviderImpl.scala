package common.mqtt

import play.api.Logger
import com.google.inject.{Inject, Singleton}
import controllers.socket.LightSocketActor
import models.{Admin, Message, Room}
import org.eclipse.paho.client.mqttv3.{IMqttActionListener, IMqttDeliveryToken, IMqttToken, MqttCallback, MqttClient, MqttConnectOptions, MqttMessage}
import play.api.libs.json.{JsDefined, JsNull, JsUndefined, Json}
import service.error.CommonErrorThrowable
import service.model.PublishMessage

import scala.util.{Failure, Success, Try}

@Singleton
class MqttServiceProviderImpl @Inject()(provider: MqttClientProvider) extends MqttServiceProvider {
  val client: MqttClient = provider.getMqttClient()
  val options = new MqttConnectOptions()
  options.setUserName(constants.CLOUD_MQTT_USERNAME)
  options.setPassword(constants.CLOUD_MQTT_PASSWORD.toCharArray)

  override def subscribeToTopic(topic: String): Unit = {
    if (!client.isConnected) {
      client.connect(options)
    }

    val subscribeCallback = new IMqttActionListener {
      override def onSuccess(asyncActionToken: IMqttToken): Unit = {
        Logger.debug(s"Subscribed!! to $topic")
      }

      override def onFailure(asyncActionToken: IMqttToken, exception: Throwable): Unit = {
        Logger.debug("Error!!")
      }
    }

    val callback = new MqttCallback {
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
        Logger.debug("Delivery complete!")
      }

      override def connectionLost(cause: Throwable): Unit = {
        Logger.debug("Connection to socket lost")
        LightSocketActor.sendMessage(PublishMessage("Connection lost"))
      }

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        val payload = message.getPayload
        Logger.debug(s"A message arrived: ${payload.mkString}")

        if (Option(payload).isEmpty) return
        if (Option(topic).isEmpty) return

        if (topic.contains("/pm/publish")) {
          val roomId = topic.substring(topic.lastIndexOf("/") + 1)

          val room = Room.findById(roomId.toLong);

          if (room.isEmpty) return

          val result = Json.parse(payload)

          var responseType: String = "";
          var oldId: Long = -1L;
          var timestamp: Long = System.currentTimeMillis();
          var chatType = "";
          var content = "";
          var ownerId = -1L;
          var recipientId = -1L;

          (result \ "type") match {
            case JsDefined(value) => responseType = value.as[String]
            case _: JsUndefined => return
          }

          (result \ "response" \ "id") match {
            case JsDefined(value) => oldId = value.as[Long]
            case _: JsUndefined => return
          }

          (result \ "response" \ "timestamp") match {
            case JsDefined(value) => timestamp = value.as[Long]
            case _: JsUndefined => return
          }

          (result \ "response" \ "chatType") match {
            case JsDefined(value) => chatType = value.as[String]
            case _: JsUndefined => return
          }

          (result \ "response" \ "content") match {
            case JsDefined(value) => content = value.as[String]
            case _: JsUndefined => return
          }

          (result \ "response" \ "ownerId") match {
            case JsDefined(value) => ownerId = value.as[Long]
            case _: JsUndefined => return
          }

          (result \ "response" \ "recipientId") match {
            case JsDefined(value) => recipientId = value.as[Long]
            case _: JsUndefined => return
          }

          val newMessage = Message(
            timestamp,
            chatType,
            content,
            read = false,
            Message.STATUS_DELIVERED,
            ownerId,
            recipientId,
            roomId.toLong
          )

          val savedMessage = Message.save(newMessage)

          val jsonResponse = Json.obj(
            "type" -> "update",
            "response" -> Json.obj(
              "oldId" -> oldId,
              "id" -> savedMessage.id,
              "timestamp" -> savedMessage.timestamp,
              "chatType" -> savedMessage.chatType,
              "content" -> savedMessage.content,
              "read" -> savedMessage.read,
              "status" -> savedMessage.status,
              "ownerId" -> savedMessage.ownerId,
              "recipientId" -> savedMessage.recipientId,
              "roomId" -> savedMessage.roomId
            )
          )

          publishToTopic(s"/pm/${roomId}", jsonResponse.toString())
          // publish to rooms topic

          val owner = Admin.findById(ownerId).get
          val recip = Admin.findById(recipientId).get

          val isOwner = if (ownerId == room.get.creatorUserId) true else false

          val ownerNewMessagesCount = Message.getUnreadMessagesCount(roomId.toLong, ownerId)

          val ownerRoomResponse = Json.obj(
            "type" -> "update",
            "response" -> Json.obj(
              "id" -> room.get.id,
              "timestamp" -> savedMessage.timestamp,
              "profileAvatar" -> room.get.profileAvatar,
              "isOnline" -> room.get.isOnline,
              "notificationsOn" -> room.get.notificationsOn,
              "chatType" -> room.get.chatType,
              "name" -> recip.name,
              "newMessagesCount" -> ownerNewMessagesCount,
              "creatorUserId" -> room.get.creatorUserId,
              "recipientUserId" -> room.get.recipientUserId,
              "lastMessage" -> Json.obj(
                "oldId" -> oldId,
                "id" -> savedMessage.id,
                "timestamp" -> savedMessage.timestamp,
                "chatType" -> savedMessage.chatType,
                "content" -> savedMessage.content,
                "read" -> savedMessage.read,
                "status" -> savedMessage.status,
                "ownerId" -> savedMessage.ownerId,
                "recipientId" -> savedMessage.recipientId,
                "roomId" -> savedMessage.roomId
              )
            )
          )

          val recipNewMessagesCount = Message.getUnreadMessagesCount(roomId.toLong, recipientId)

          val recipRoomResponse = Json.obj(
            "type" -> "update",
            "response" -> Json.obj(
              "id" -> room.get.id,
              "timestamp" -> savedMessage.timestamp,
              "profileAvatar" -> room.get.profileAvatar,
              "isOnline" -> room.get.isOnline,
              "notificationsOn" -> room.get.notificationsOn,
              "chatType" -> room.get.chatType,
              "name" -> owner.name,
              "newMessagesCount" -> recipNewMessagesCount,
              "creatorUserId" -> room.get.creatorUserId,
              "recipientUserId" -> room.get.recipientUserId,
              "lastMessage" -> Json.obj(
                "oldId" -> oldId,
                "id" -> savedMessage.id,
                "timestamp" -> savedMessage.timestamp,
                "chatType" -> savedMessage.chatType,
                "content" -> savedMessage.content,
                "read" -> savedMessage.read,
                "status" -> savedMessage.status,
                "ownerId" -> savedMessage.ownerId,
                "recipientId" -> savedMessage.recipientId,
                "roomId" -> savedMessage.roomId
              )
            )
          )
          publishToTopic(s"/room/${ownerId}", ownerRoomResponse.toString())
          publishToTopic(s"/room/${recipientId}", recipRoomResponse.toString())
        } else if (topic.contains("/pm/seen")) {
          val roomId = topic.substring(topic.lastIndexOf("/") + 1)

          val room = Room.findById(roomId.toLong);
          if (room.isEmpty) return

          val result = Json.parse(payload)
          var messageType = ""
          var messageId = -1L
          var seen = false

          (result \ "type") match {
            case JsDefined(value) => messageType = value.as[String]
            case _: JsUndefined => return
          }

          (result \ "seen") match {
            case JsDefined(value) => seen = value.as[Boolean]
            case _: JsUndefined => return
          }

          if ("seen".equals(messageType)) {
            (result \ "messageId") match {
              case JsDefined(value) => messageId = value.as[Long]
              case _: JsUndefined => return
            }

            val jsonResponse = Json.obj(
              "type" -> "seen",
              "response" -> Json.obj(
                "messageId" -> messageId
              )
            )
            publishToTopic(s"/pm/${roomId}", jsonResponse.toString())

            val room = Room.findById(roomId.toLong).get

            val roomResponse = Json.obj(
              "type" -> "seen",
              "response" -> Json.obj(
                "seen" -> true,
                "roomId" -> roomId.toLong
              )
            )
            publishToTopic(s"/room/${room.creatorUserId}", roomResponse.toString())
            publishToTopic(s"/room/${room.recipientUserId}", roomResponse.toString())
          } else if ("allseen".equals(messageType)) {
            // all seen
            var userId = -1L

            (result \ "userId") match {
              case JsDefined(value) => userId = value.as[Long]
              case _: JsUndefined => return
            }

            Message.getMessagesByRoomIdWithUserId(roomId.toLong, userId).foreach(message => {
              Message.updateFields(message, read = true, Message.STATUS_SEEN)
            })
            
            val jsonResponse = Json.obj(
              "type" -> "allseen",
              "response" -> Json.obj(
                "allseen" -> true,
                "userId" -> userId
              )
            )

            publishToTopic(s"/pm/${roomId}", jsonResponse.toString())

            val room = Room.findById(roomId.toLong).get

            val roomResponse = Json.obj(
              "type" -> "seen",
              "response" -> Json.obj(
                "seen" -> true,
                "roomId" -> roomId.toLong
              )
            )
            publishToTopic(s"/room/${room.creatorUserId}", roomResponse.toString())
            publishToTopic(s"/room/${room.recipientUserId}", roomResponse.toString())
          }
        }
      }
    }

    client.subscribeWithResponse(topic).setActionCallback(subscribeCallback)
    client.setCallback(callback)
  }

  override def publishToTopic(topic: String, message: String): Boolean = {
    if (!client.isConnected) {
      client.connect(options)
    }

    val result = Try(client.getTopic(topic)) match {
      case Success(messageTopic) =>
        val mqttMessage = new MqttMessage(message.getBytes("utf-8"))
        Try(messageTopic.publish(mqttMessage)) match {
          case Success(r) =>
            play.Logger.debug(s"Result of publishing: ${r.getMessage}")
            true
          case Failure(exception) =>

            play.Logger.error(
              s"""
                 |An error occurred while trying to publish the message "$message" to $topic into ${constants.BROKER_URL}:
         """.stripMargin, exception)
            false
        }
      case Failure(exception) =>
        play.Logger.error(
          s"""
             |An error occurred while trying to publish the message "$message" to $topic into ${constants.BROKER_URL}:
         """.stripMargin, exception)
        false
    }
    result
  }
}
