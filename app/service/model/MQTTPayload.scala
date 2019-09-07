package service.model

import play.api.libs.json.{Format, Json}


case class MQTTPayload(id: Long, contentType: String, payload: String);

object MQTTPayload {
  implicit val format: Format[MQTTPayload] = Json.format[MQTTPayload]
}