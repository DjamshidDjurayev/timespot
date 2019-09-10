package common.mqtt

trait MqttServiceProvider {
  def subscribeToTopic(topic: String): Unit

  def publishToTopic(topic: String, message: String): Boolean
}
