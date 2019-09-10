package common.modules

import com.tzavellas.sse.guice.ScalaModule
import common.cloudinary.{CloudinaryProvider, CloudinaryProviderImpl}
import common.ws.{WsProvider, WsProviderImpl}

class Module extends ScalaModule {
  override def configure(): Unit = {
    bind[WsProvider].to[WsProviderImpl].asEagerSingleton()
    bind[CloudinaryProvider].to[CloudinaryProviderImpl].asEagerSingleton()
  }
}
