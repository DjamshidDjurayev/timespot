package service

import com.google.inject.ImplementedBy
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttMessage}
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * PublishService
  */
object PublishService {
  val client = new MqttClient(brokerUrl, MqttClient.generateClientId(), persistence)
  def publish(message: String): Boolean = {
    client.connect()
    val result = Try(client.getTopic(topic+"/set")) match {
      case Success(messageTopic) =>
        val mqttMessage = new MqttMessage(message.getBytes("utf-8"))
        Try(messageTopic.publish(mqttMessage)) match {
          case Success(r) =>
            play.Logger.debug(s"Result of publishing: ${r.getMessage}")
            true
          case Failure(exception) =>

            play.Logger.error(
              s"""
                 |An error occurred while trying to publish the message "$message" to $topic into $brokerUrl:
         """.stripMargin, exception)
            false
        }
      case Failure(exception) =>
        play.Logger.error(s"""
                             |An error occurred while trying to publish the message "$message" to $topic into $brokerUrl:
         """.stripMargin, exception)
        false
    }
    client.disconnect()
    result
  }
}
