package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Inject, Singleton}
import common.mqtt.MqttServiceProvider
import controllers.socket.LightSocketActor
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import service.model.PublishMessage

@Singleton
class Application @Inject()(implicit system: ActorSystem,
                            materializer: Materializer,
                            mqttServiceProvider: MqttServiceProvider,
                            components: ControllerComponents) extends AbstractController(components) {
  mqttServiceProvider.subscribeToTopic(constants.topic)

  def socket: WebSocket = WebSocket.accept[PublishMessage, PublishMessage] { _ =>
    ActorFlow.actorRef(out => LightSocketActor.props(out))
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.main2("as"))
  }
}
