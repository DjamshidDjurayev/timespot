package common.mqtt

import com.google.inject.{Inject, Singleton}
import controllers.socket.LightSocketActor
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttConnectOptions, MqttMessage}
import service.model.Message

import scala.util.{Failure, Success, Try}

@Singleton
class MqttServiceProviderImpl @Inject()(provider: MqttClientProvider) extends MqttServiceProvider {
  val client: MqttClient = provider.getMqttClient()
  val options = new MqttConnectOptions()
  options.setUserName(constants.CLOUD_MQTT_USERNAME)
  options.setPassword(constants.CLOUD_MQTT_PASSWORD.toCharArray)

  override def subscribeToTopic(topic: String): Unit = {
    if (!client.isConnected) {
      client.connect(options)
    }
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
//        LightSocketActor.sendMessage(Message(new String(message.getPayload)))
      }
    }
    client.setCallback(callback)
  }

  override def publishToTopic(topic: String, message: String): Boolean = {
    if (!client.isConnected) {
      client.connect(options)
    }

    val result = Try(client.getTopic(topic)) match {
      case Success(messageTopic) =>
        val mqttMessage = new MqttMessage(message.getBytes("utf-8"))
        Try(messageTopic.publish(mqttMessage)) match {
          case Success(r) =>
            play.Logger.debug(s"Result of publishing: ${r.getMessage}")
            true
          case Failure(exception) =>

            play.Logger.error(
              s"""
                 |An error occurred while trying to publish the message "$message" to $topic into ${constants.BROKER_URL}:
         """.stripMargin, exception)
            false
        }
      case Failure(exception) =>
        play.Logger.error(
          s"""
             |An error occurred while trying to publish the message "$message" to $topic into ${constants.BROKER_URL}:
         """.stripMargin, exception)
        false
    }

    if (client.isConnected) {
      client.disconnect()
    }
    result
  }
}
