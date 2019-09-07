import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  */
package object service {
  val persistence: MemoryPersistence = new MemoryPersistence // MqttDefaultFilePersistence("/tmp")
  val topic: String = "xamidova/feed/topic"
}
