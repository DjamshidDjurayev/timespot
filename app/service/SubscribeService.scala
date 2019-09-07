package service

import controllers.socket.LightSocketActor
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttConnectOptions, MqttMessage}
import service.model.Message

/**
  */
object SubscribeService {
  val client = new MqttClient(constants.BROKER_URL, MqttClient.generateClientId(), persistence)
  val options = new MqttConnectOptions()
  options.setUserName(constants.CLOUD_MQTT_USERNAME)
  options.setPassword(constants.CLOUD_MQTT_PASSWORD.toCharArray)

  def subscribe(): Unit = {
    client.connect(options)
    client.subscribe(topic)

    val callback = new MqttCallback {
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
        play.Logger.debug("Delivery complete!")
      }

      override def connectionLost(cause: Throwable): Unit = {
        play.Logger.debug("Connection to socket lost")
        LightSocketActor.sendMessage(Message("Connection lost"))
      }

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        play.Logger.debug(s"A message arrived: ${new String(message.getPayload)}")
        LightSocketActor.sendMessage(Message(new String(message.getPayload)))
      }
    }

    client.setCallback(callback)
  }
}
