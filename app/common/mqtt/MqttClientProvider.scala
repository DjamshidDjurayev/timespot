package common.mqtt

import org.eclipse.paho.client.mqttv3.MqttClient

trait MqttClientProvider {
  def getMqttClient(): MqttClient
}
