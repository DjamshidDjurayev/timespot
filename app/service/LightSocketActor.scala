package controllers.socket

import akka.actor.{Actor, ActorRef, Props}
import service.model.PublishMessage
import service.PublishService

import scala.collection.mutable.ListBuffer

/**
 * LightSocketActor
 */
class LightSocketActor(out: ActorRef) extends Actor {
  val topic: String = service.topic

  override def receive: Receive = {
    case message: PublishMessage =>
      play.Logger.debug(s"Message: ${message.information}")
      PublishService.publish(message.information)
    // out ! message
  }
}

object LightSocketActor {
  var list: ListBuffer[ActorRef] = ListBuffer.empty[ActorRef]
  def props(out: ActorRef): Props = {
    list += out
    Props(new LightSocketActor(out))
  }

  def sendMessage(message: PublishMessage): Unit = {
    list.foreach(_ ! message)
  }
}
