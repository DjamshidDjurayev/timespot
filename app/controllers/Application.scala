package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import controllers.socket.LightSocketActor
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import service.SubscribeService
import service.model.Message

@Singleton
class Application @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {
  SubscribeService.subscribe()
  def socket: WebSocket = WebSocket.accept[Message, Message] { _ =>
    ActorFlow.actorRef(out => LightSocketActor.props(out))
  }

  def index: Action[AnyContent] = Action {
    Ok(views.html.main2("as"))
  }
}
