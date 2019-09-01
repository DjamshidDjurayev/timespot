  package controllers

  import models.{Db, Device, Positions, Staffer}
  import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
  import play.api.mvc._

  /**
   * Created by dzhuraev on 5/12/16.
   */
  class DeviceController extends Controller {

    def registerDevice(deviceId: String, tokenId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId, tokenId).map {
        device => {
          Ok(Json.obj("status" -> "fail", "message" -> "device already exists"))
        }
      }.getOrElse {
        Device.saveDevice(deviceId, tokenId)
        Ok(Json.obj("status" -> "success", "message" -> "device saved"))
      }
    }

    def registerDevicePost(): Action[AnyContent] = Action { implicit request =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson

      jsonBody.map { json => {
        val deviceId = (json \ "deviceId").as[String]
        val token = (json \ "token").as[String]
        Device.saveDevice(deviceId, token)
        Ok(Json.obj("status" -> "success", "message" -> "Device added successfully"))
      }
      }.getOrElse {
        BadRequest("Expecting application/json request body")
      }
    }

    def removeDevice(deviceId: String, tokenId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId, tokenId).map {
        device => {
          Device.removeDevice(device)
          Ok(Json.obj("status" -> "success", "message" -> "device removed"))
        }
      }.getOrElse {
        Ok(Json.obj("status" -> "fail", "message" -> "device not found"))
      }
    }

    def updateDevice(deviceId: String, tokenId: String): Action[AnyContent] = Action {
      Device.findDevice(deviceId, tokenId).map {
        device => {
          Device.updateDevice(device)
          Ok(Json.obj("status" -> "success", "message" -> "device updated"))
        }
      }.getOrElse {
        Ok(Json.obj("status" -> "fail", "message" -> "device not found"))
      }
    }

    def getAllDevices = Action {
      val devices = Device.getAllDevices()

      val list = devices.map {
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

    def removeAllDevices = Action {
      Device.removeAllDevices()
      Ok(Json.obj("status" -> "success", "message" -> "devices removed"))
    }
  }
