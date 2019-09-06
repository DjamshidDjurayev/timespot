import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by camilosampedro on 29/03/17.
  */
package object service {
  val persistence = new MemoryPersistence // MqttDefaultFilePersistence("/tmp")
  val brokerUrl: String = "tcp://localhost:1883"

  val topic = "homie/lucecilla/light/on"
}
