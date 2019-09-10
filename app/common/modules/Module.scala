package common.modules

import com.tzavellas.sse.guice.ScalaModule
import common.cloudinary.{CloudinaryProvider, CloudinaryProviderImpl}
import common.mqtt.{MqttClientImpl, MqttClientProvider, MqttServiceProvider, MqttServiceProviderImpl}
import common.fcm.{FcmProvider, FcmProviderImpl}

class Module extends ScalaModule {
  override def configure(): Unit = {
    bind[FcmProvider].to[FcmProviderImpl].asEagerSingleton()
    bind[CloudinaryProvider].to[CloudinaryProviderImpl].asEagerSingleton()
    bind[MqttClientProvider].to[MqttClientImpl].asEagerSingleton()
    bind[MqttServiceProvider].to[MqttServiceProviderImpl].asEagerSingleton()
  }
}
