package common.ws
import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WsProviderImpl @Inject()(implicit executionContext: ExecutionContext, ws: WSClient) extends WsProvider {
  private val key: String = constants.FCMKey

  override def send(ids: List[String], data: Map[String, String]): Future[Unit] = {
    val body = Json.obj(
      "registration_ids" -> ids,
      "data" -> data
    )

    ws.url(constants.FCMUrl)
      .withHeaders(
        "Authorization" -> s"key=$key",
        "Content-type" -> "application/json"
      )
      .post(body)
      .map { response =>
        Logger.debug("Result: " + response.body)
      }
  }
}
