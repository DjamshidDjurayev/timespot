package common.mqtt
import com.google.inject.Singleton
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

@Singleton
class MqttClientImpl extends MqttClientProvider {
  override def getMqttClient(): MqttClient = {
    val persistence: MemoryPersistence = new MemoryPersistence
    val mqttClient = new MqttClient(constants.BROKER_URL, MqttClient.generateClientId(), persistence)
    mqttClient
  }
}
