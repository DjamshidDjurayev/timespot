package service.model

import play.api.libs.json.{Format, Json}
import play.api.mvc.WebSocket.MessageFlowTransformer

/**
 * Message
 */
case class PublishMessage(information: String)

object PublishMessage {
  implicit val messageFormat: Format[PublishMessage] = Json.format[PublishMessage]
  implicit val messageFlowTransformer: MessageFlowTransformer[PublishMessage, PublishMessage] = MessageFlowTransformer.jsonMessageFlowTransformer[PublishMessage, PublishMessage]
}