package service.model

import play.api.libs.json.{Format, Json}
import play.api.mvc.WebSocket.MessageFlowTransformer

/**
 * Message
 */
case class Message(information: String)

object Message {
  implicit val messageFormat: Format[Message] = Json.format[Message]
  implicit val messageFlowTransformer: MessageFlowTransformer[Message, Message] = MessageFlowTransformer.jsonMessageFlowTransformer[Message, Message]
}