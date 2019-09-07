package service

import org.eclipse.paho.client.mqttv3.{MqttClient, MqttMessage}

import scala.util.{Failure, Success, Try}

/**
  * PublishService
  */
object PublishService {
  val client = new MqttClient(constants.BROKER_URL, MqttClient.generateClientId(), persistence)

  def publish(message: String): Boolean = {
    client.connect()
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
        play.Logger.error(s"""
                             |An error occurred while trying to publish the message "$message" to $topic into ${constants.BROKER_URL}:
         """.stripMargin, exception)
        false
    }
    client.disconnect()
    result
  }
}
