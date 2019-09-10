package common.fcm
import com.google.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FcmProviderImpl @Inject()(implicit executionContext: ExecutionContext, ws: WSClient) extends FcmProvider {
  private val key: String = constants.FCMKey

  override def send(ids: List[String], data: Map[String, String]): Future[WSResponse] = {
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
  }
}
