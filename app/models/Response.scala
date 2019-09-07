package models

import play.api.libs.json.{Json, OFormat}

/**
 * Created by dzhuraev on 4/22/16.
 */
case class Response(status: String, message: String)

object Response {
  implicit val responseFormat: OFormat[Response] = Json.format[Response]
}