package common.ws

import scala.concurrent.Future

trait WsProvider {
  def send(ids: List[String], data: Map[String, String]): Future[Unit]
}
