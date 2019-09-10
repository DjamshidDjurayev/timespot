package common.fcm

import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait FcmProvider {
  def send(ids: List[String], data: Map[String, String]): Future[WSResponse]
}
