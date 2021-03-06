  package controllers

  import com.google.inject.Inject
  import models.Device
  import play.api.libs.json.{JsValue, Json}
  import play.api.mvc._

  /**
   * Created by dzhuraev on 5/12/16.
   */
  class DeviceController @Inject()(components: ControllerComponents) extends AbstractController(components) {

    def registerDevice(deviceId: String, tokenId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId).map {
        _ => {
          BadRequest(Json.obj("status" -> "fail", "message" -> "Device already exists"))
        }
      }.getOrElse {
        Device.saveDevice(deviceId, tokenId)
        Ok(Json.obj("status" -> "success", "message" -> "Device saved"))
      }
    }

    def registerDevicePost(): Action[AnyContent] = Action { implicit request =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson

      jsonBody.map { json => {
        val deviceId = (json \ "deviceId").as[String]
        val token = (json \ "token").as[String]

        Device.findDevice(deviceId).map {
          device => {
            Device.updateDeviceByDeviceId(device, token)
            Ok(Json.obj("status" -> "success", "message" -> "Device updated successfully"))
          }
        }.getOrElse {
          Device.saveDevice(deviceId, token)
          Ok(Json.obj("status" -> "success", "message" -> "Device added successfully"))
        }
      }
      }.getOrElse {
        BadRequest(Json.obj("status" -> "fail", "message" -> "Error while parsing json body"))
      }
    }

    def removeDevice(deviceId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId).map {
        device => {
          Device.removeDevice(device)
          Ok(Json.obj("status" -> "success", "message" -> "Device removed"))
        }
      }.getOrElse {
        BadRequest(Json.obj("status" -> "fail", "message" -> "Device not found"))
      }
    }

    def updateDevice(deviceId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId).map {
        device => {
          Device.updateDevice(device)
          Ok(Json.obj("status" -> "success", "message" -> "Device updated"))
        }
      }.getOrElse {
        BadRequest(Json.obj("status" -> "fail", "message" -> "Device not found"))
      }
    }

    def getAllDevices: Action[AnyContent] = Action {
      val list = Device.getAllDevices().map {
        device => {
          Json.obj(
            "id" -> device.id,
            "deviceId" -> device.deviceId,
            "token" -> device.token
          )
        }
      }
      Ok(Json.toJson(list))
    }

    def removeAllDevices(): Action[AnyContent] = Action {
      Device.removeAllDevices()
      Ok(Json.obj("status" -> "success", "message" -> "Device removed successfully"))
    }
  }
