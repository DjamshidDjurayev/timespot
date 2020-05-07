package controllers

import com.google.inject.Inject
import common.mqtt.MqttServiceProvider
import models.{Admin, Message, Room}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsDefined, JsNull, JsUndefined, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.error.{CommonErrorThrowable, NotAuthorizedThrowable}

import scala.concurrent.ExecutionContext

class ChatController @Inject()(implicit context: ExecutionContext,
                               mqttServiceProvider: MqttServiceProvider,
                               components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  def createChat(): Action[AnyContent] = Action { implicit request =>
    try {
      val token = request.headers.get("jwt_token")

      token.map { token => {
        Admin.findAdminByToken(token).map {
          admin => {
            val body: AnyContent = request.body
            val jsonBody: Option[JsValue] = body.asJson

            jsonBody.map { json => {
              var ownerId: Long = -1L;
              var recipientId: Long = -1L;
              var chatType: String = ""

              (json \ "userId") match {
                case JsDefined(value) => ownerId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "recipientId") match {
                case JsDefined(value) => recipientId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "chatType") match {
                case JsDefined(value) => chatType = value.as[String]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              if (ownerId == recipientId) {
                throw CommonErrorThrowable("Owner cannot be recipient")
              }

              val creator = Admin.findById(ownerId)
              val recipient = Admin.findById(recipientId)

              if (creator.isEmpty) {
                throw NotAuthorizedThrowable("Not authorized")
              }

              if (recipient.isEmpty) {
                throw CommonErrorThrowable("Recipient not found")
              }

              if (ownerId != admin.id) {
                throw NotAuthorizedThrowable("Not authorized")
              }

              val isOwner = if (Room.getRoomByRecipientId(ownerId, recipientId).isDefined) true else false
              val firstArg = if (isOwner) ownerId else recipientId
              val secondArg = if (isOwner) recipientId else ownerId

              if (Room.getRoomByRecipientId(firstArg, secondArg).isDefined) {
                throw CommonErrorThrowable("Chat already exists")
              }

              val ownerRoom = new Room(
                System.currentTimeMillis(),
                "",
                false,
                true,
                chatType,
                recipient.get.name,
                0,
                ownerId,
                recipientId
              )

              val saved = Room.save(ownerRoom)
              val name: String = if (isOwner) recipient.get.name else creator.get.name

              val jsonResponse = Json.obj(
                "type" -> "add",
                "response" -> Json.obj(
                  "id" -> saved.id,
                  "timestamp" -> saved.timestamp,
                  "profileAvatar" -> saved.profileAvatar,
                  "isOnline" -> saved.isOnline,
                  "notificationsOn" -> saved.notificationsOn,
                  "chatType" -> saved.chatType,
                  "name" -> recipient.get.name,
                  "newMessagesCount" -> saved.newMessagesCount,
                  "creatorUserId" -> saved.creatorUserId,
                  "recipientUserId" -> saved.recipientUserId,
                  "lastMessage" -> JsNull
                )
              )

              val jsonResponse2 = Json.obj(
                "type" -> "add",
                "response" -> Json.obj(
                  "id" -> saved.id,
                  "timestamp" -> saved.timestamp,
                  "profileAvatar" -> saved.profileAvatar,
                  "isOnline" -> saved.isOnline,
                  "notificationsOn" -> saved.notificationsOn,
                  "chatType" -> saved.chatType,
                  "name" -> creator.get.name,
                  "newMessagesCount" -> saved.newMessagesCount,
                  "creatorUserId" -> saved.creatorUserId,
                  "recipientUserId" -> saved.recipientUserId,
                  "lastMessage" -> JsNull
                )
              )

              mqttServiceProvider.publishToTopic(s"/room/${ownerId}", jsonResponse.toString())
              mqttServiceProvider.publishToTopic(s"/room/${recipientId}", jsonResponse2.toString())

              Ok(Json.toJson(Json.obj(
                "code" -> 200,
              "status" -> "Chat created"
              )))
            }
            }.getOrElse {
              BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
            }
          }
        }.getOrElse {
          Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
        }
      }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    } catch {
      case throwable: CommonErrorThrowable => BadRequest(Json.obj("status" -> "fail", "message" -> throwable.getMessage))
      case notAuthorizedThrowable: NotAuthorizedThrowable => Unauthorized(Json.obj("status" -> 401, "message" -> notAuthorizedThrowable.getMessage))
      case error: Throwable => BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error"))
    }
  }

  def getAllChatRooms: Action[AnyContent] = Action { implicit request =>
    val chatRooms = Room.getAllRooms()
    val chatRoomsList = chatRooms.map {
      room => {
        Json.obj(
          "id" -> room.id,
          "timestamp" -> room.timestamp,
          "profileAvatar" -> room.profileAvatar,
          "isOnline" -> room.isOnline,
          "notificationsOn" -> room.notificationsOn,
          "chatType" -> room.chatType,
          "name" -> room.name,
          "newMessagesCount" -> room.newMessagesCount,
          "creatorUserId" -> room.creatorUserId,
          "recipientUserId" -> room.recipientUserId
        )
      }
    }

    val roomsResponse = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> chatRoomsList
    )

    Ok(Json.toJson(roomsResponse))
  }

  def getChatRooms: Action[AnyContent] = Action { implicit request =>
    try {
      val token = request.headers.get("jwt_token")

      token.map {
        token => {
          Admin.findAdminByToken(token).map {
            admin => {
              val chatRooms = Room.getRoomsByRecipientId(admin.id)
              val chatRoomsList = chatRooms.map {
                room => {
                  val isOwner = if (admin.id == room.creatorUserId) true else false
                  val owner: Admin = Admin.findById(room.creatorUserId).get
                  val recipient: Admin = Admin.findById(room.recipientUserId).get
                  val lastMessage = Message.getLastMessageByRoomId(room.id)
                  val name: String = if (isOwner) recipient.name else owner.name

                  val newMessagesCount = Message.getUnreadMessagesCount(room.id, admin.id)

                  Json.obj(
                    "id" -> room.id,
                    "timestamp" -> {
                      if (lastMessage.isEmpty) room.timestamp else lastMessage.get.timestamp
                    },
                    "profileAvatar" -> room.profileAvatar,
                    "isOnline" -> room.isOnline,
                    "notificationsOn" -> room.notificationsOn,
                    "chatType" -> room.chatType,
                    "name" -> name,
                    "newMessagesCount" -> newMessagesCount,
                    "creatorUserId" -> room.creatorUserId,
                    "recipientUserId" -> room.recipientUserId,
                    "lastMessage" -> {
                      if (lastMessage.isEmpty) JsNull else lastMessage.get
                    }
                  )
                }
              }

              val roomsResponse = Json.obj(
                "code" -> 200,
                "status" -> "success",
                "data" -> chatRoomsList
              )

              Ok(Json.toJson(roomsResponse))
            }
          }.getOrElse {
            Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
          }
        }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    } catch {
      case throwable: CommonErrorThrowable => BadRequest(Json.obj("status" -> "fail", "message" -> throwable.getMessage))
      case notAuthorizedThrowable: NotAuthorizedThrowable => Unauthorized(Json.obj("status" -> 401, "message" -> notAuthorizedThrowable.getMessage))
      case error: Throwable => BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error", "axax" -> error.getMessage))
    }
  }

  def createMessage: Action[AnyContent] = Action { implicit request =>
    try {
      val token = request.headers.get("jwt_token")

      token.map { token => {
        Admin.findAdminByToken(token).map {
          _ => {
            val body: AnyContent = request.body
            val jsonBody: Option[JsValue] = body.asJson

            jsonBody.map { json => {
              var ownerId: Long = -1L;
              var recipientId: Long = -1L;
              var roomId: Long = -1L;
              var chatType: String = ""
              var content: String = ""
              var read: Boolean = false;
              var status: Int = Message.STATUS_PENDING

              (json \ "chatType") match {
                case JsDefined(value) => chatType = value.as[String]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "content") match {
                case JsDefined(value) => content = value.as[String]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "read") match {
                case JsDefined(value) => read = value.as[Boolean]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "status") match {
                case JsDefined(value) => status = value.as[Int]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "ownerId") match {
                case JsDefined(value) => ownerId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "recipientId") match {
                case JsDefined(value) => recipientId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "roomId") match {
                case JsDefined(value) => roomId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              if (ownerId == recipientId) {
                throw CommonErrorThrowable("You can't send messages to yourself")
              }

              val creator = Admin.findById(ownerId)
              val recipient = Admin.findById(recipientId)

              if (creator.isEmpty) {
                throw NotAuthorizedThrowable("Not authorized")
              }

              if (recipient.isEmpty) {
                throw CommonErrorThrowable("Recipient not found")
              }

              var belongToRoom = false
              val room = Room.findById(roomId)

              if (room.isEmpty) {
                throw CommonErrorThrowable("Room not found")
              }

              if (room.get.creatorUserId == ownerId && room.get.recipientUserId == recipientId) {
                belongToRoom = true
              }

              if (!belongToRoom && room.get.creatorUserId == recipientId && room.get.recipientUserId == ownerId) {
                belongToRoom = true
              }

              if (!belongToRoom) {
                throw CommonErrorThrowable("Wrong chat")
              }

              val message = new Message(
                System.currentTimeMillis(),
                chatType,
                content,
                read,
                status,
                ownerId,
                recipientId,
                roomId,
                false
              )

              val savedMessage = Message.save(message)
              if (Option(savedMessage).isDefined) {
                Ok(Json.toJson(
                  Json.obj(
                    "code" -> 200,
                "status" -> "Message created"
                  )
                ))
              } else {
                throw CommonErrorThrowable("Something went wrong")
              }
            }
            }.getOrElse {
              BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
            }
          }
        }.getOrElse {
          Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
        }
      }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    } catch {
      case throwable: CommonErrorThrowable => BadRequest(Json.obj("status" -> "fail", "message" -> throwable.getMessage))
      case notAuthorizedThrowable: NotAuthorizedThrowable => Unauthorized(Json.obj("status" -> 401, "message" -> notAuthorizedThrowable.getMessage))
      case error: Throwable => BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error"))
    }
  }

  def editMessage: Action[AnyContent] = Action { implicit request =>
    try {
      val token = request.headers.get("jwt_token")

      token.map { token => {
        Admin.findAdminByToken(token).map {
          _ => {
            val body: AnyContent = request.body
            val jsonBody: Option[JsValue] = body.asJson

            jsonBody.map { json => {
              var ownerId: Long = -1L;
              var messageId: Long = -1L;
              var roomId: Long = -1L;
              var content: String = "";

              (json \ "ownerId") match {
                case JsDefined(value) => ownerId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "messageId") match {
                case JsDefined(value) => messageId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "roomId") match {
                case JsDefined(value) => roomId = value.as[Long]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              (json \ "message") match {
                case JsDefined(value) => content = value.as[String]
                case _: JsUndefined => throw CommonErrorThrowable("Missing property")
              }

              val room = Room.findById(roomId)

              if (room.isEmpty) {
                throw CommonErrorThrowable("Room not found")
              }

              val owner = Admin.findById(ownerId)

              if (owner.isEmpty) {
                throw NotAuthorizedThrowable("Not authorized")
              }

              val editedMessage = Message.findById(messageId)

              if (editedMessage.isEmpty) {
                throw CommonErrorThrowable("Message not found")
              }

              val savedMessage = Message.updateContent(editedMessage.get, content)

              if (Option(savedMessage).isDefined) {
                val jsonResponse = Json.obj(
                  "type" -> "edit",
                  "response" -> Json.obj(
                    "ownerId" -> ownerId,
                    "messageId" -> messageId,
                    "updatedMessage" -> content
                  )
                )

                mqttServiceProvider.publishToTopic(s"/pm/${roomId}", jsonResponse.toString())

                Ok(Json.toJson(
                  Json.obj(
                    "code" -> 200,
                "status" -> "Message edited"
                  )
                ))
              } else {
                throw CommonErrorThrowable("Something went wrong")
              }
            }
            }.getOrElse {
              BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
            }
          }
        }.getOrElse {
          Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
        }
      }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    } catch {
      case throwable: CommonErrorThrowable => BadRequest(Json.obj("status" -> "fail", "message" -> throwable.getMessage))
      case notAuthorizedThrowable: NotAuthorizedThrowable => Unauthorized(Json.obj("status" -> 401, "message" -> notAuthorizedThrowable.getMessage))
      case error: Throwable => BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error"))
    }
  }

  def getChatMessages(roomId: Long, messageId: Long, limit: Int): Action[AnyContent] = Action { implicit request =>
    try {
      val token = request.headers.get("jwt_token")

      if (Option(roomId).isEmpty) {
        throw CommonErrorThrowable("Room id is required")
      }

      if (Option(messageId).isEmpty) {
        throw CommonErrorThrowable("messageId is required")
      }

      if (Option(limit).isEmpty) {
        throw CommonErrorThrowable("Limit is required")
      }

      token.map {
        token => {
          Admin.findAdminByToken(token).map {
            _ => {

              val room = Room.findById(roomId)

              if (room.isEmpty) {
                throw CommonErrorThrowable("Room not found")
              }

              val messages = Message.getMessagesByRoomIdAndMessageId(roomId, messageId, limit)
              val messagesList = messages.map {
                message => {
                  Json.obj(
                    "id" -> message.id,
                    "timestamp" -> message.timestamp,
                    "chatType" -> message.chatType,
                    "content" -> message.content,
                    "read" -> message.read,
                    "status" -> message.status,
                    "ownerId" -> message.ownerId,
                    "recipientId" -> message.recipientId,
                    "roomId" -> message.roomId,
                    "edited" -> message.edited
                  )
                }
              }

              val messagesResponse = Json.obj(
                "code" -> 200,
                "status" -> "success",
                "data" -> messagesList
              )

              Ok(Json.toJson(messagesResponse))
            }
          }.getOrElse {
            Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
          }
        }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    } catch {
      case throwable: CommonErrorThrowable => BadRequest(Json.obj("status" -> "fail", "message" -> throwable.getMessage))
      case notAuthorizedThrowable: NotAuthorizedThrowable => Unauthorized(Json.obj("status" -> 401, "message" -> notAuthorizedThrowable.getMessage))
      case error: Throwable => BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error", "cause" -> error.getMessage))
    }
  }
}
