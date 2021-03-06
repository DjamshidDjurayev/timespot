package controllers

import common.fcm.FcmProvider
import common.mqtt.MqttServiceProvider
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class Staff @Inject()(implicit context: ExecutionContext,
                      fcmProvider: FcmProvider,
                      mqttServiceProvider: MqttServiceProvider,
                      components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.Staff.list(0, 2, ""))

  def getStaff: Action[AnyContent] = Action {
    val staffs = Db.query[Staffer].order("id", reverse = true).fetch()
    Ok(Json.toJson(staffs))
  }

  def getOneStaff(id: Long): Action[AnyContent] = Action {
    Ok(Json.toJson(Staffer.findById(id)))
  }

  def getStaffByQrCode(code: String): Action[AnyContent] = Action {
    Ok(Json.toJson(Staffer.findByQrCode(code)))
  }

  def list(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    val rooms = Room.getAllRooms()
    rooms.foreach(room => {
      // subscribe to rooms
      mqttServiceProvider.subscribeToTopic(s"/pm/publish/${room.id}")
      mqttServiceProvider.subscribeToTopic(s"/pm/seen/${room.id}")
    })

    Ok(views.html.staff(
      Staffer.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Staffer.delete(id)
    Home.flashing("success" -> "User has been deleted")
  }

  def addHistory(code: String): Action[AnyContent] = Action {
    val firstNames = Device.getAllDevices().map(_.token).toList
    val actionDateTime = DateTime.now().getMillis

    Staffer.findByQrCode(code).map {
      staffer => {
        val counter: Int = StaffHistory.historyCount(staffer, actionDateTime)
        counter match {
          case 0 =>
            val history = StaffHistory(staffer, 0, actionDateTime, actionDateTime)
            Db.save[StaffHistory](history)

            fcmProvider.send(firstNames, Map(
              "message" ->" пришел",
              "name" -> staffer.name,
              "surname" -> staffer.surname
            ))

            Ok(Json.obj("status" -> "success", "count" -> counter, "staff" -> Json.toJson(staffer)))
          case 1 =>
            val history = StaffHistory(staffer, 1, actionDateTime, actionDateTime)
            Db.save[StaffHistory](history)

            fcmProvider.send(firstNames, Map(
              "message" ->" ушел",
              "name" -> staffer.name,
              "surname" -> staffer.surname
            ))

            Ok(Json.obj("status" -> "success", "count" -> counter, "staff" -> Json.toJson(staffer)))
          case 2 =>
            Ok(Json.obj("status" -> "fail", "count" -> counter,"staff" -> Json.toJson(staffer)))
        }
      }
    }.getOrElse {
      Ok(Json.obj("status" -> "fail", "count" -> -1))
    }
  }


  def getHistory(code: String): Action[AnyContent] = Action {
    Staffer.findByQrCode(code).map {
      history => {
        Ok(Json.toJson(StaffHistory.findById(history)))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Staff not found"))
    }
  }

  def getActionsByDate(userId: String, date: Long): Action[AnyContent] = Action {
    Staffer.findByQrCode(userId).map {
      staff => {
        Ok(Json.toJson(StaffHistory.getStaffActionsByDate(staff, date)))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Staff not found"))
    }
  }

  def checkCode(): Action[AnyContent] = Action {
    var isExist = true
    var totalResult = ""

    do {
      val generatedCode = BearerTokenGenerator.generateMD5Token()
      Staffer.findByQrCode(generatedCode).map {
        _ => {
          isExist = true
        }
      }.getOrElse {
        isExist = false
        totalResult = generatedCode
      }
    } while(isExist)

    Ok(totalResult)
  }

  def deleteOneStaff(id: String): Action[AnyContent] = Action {
    Staffer.findByQrCode(id).map {
      staff => {
        Staffer.deleteByCode(staff)
        Ok(Json.obj("status" -> "success", "message" -> "Staff removed"))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Staff not found"))
    }
  }

  // handling POST request  from client side
  def addNewStaff(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val name = (json \ "name").as[String]
      val image = (json \ "image").as[String]
      val birth = (json \ "birth").as[Long]
      val surname = (json \ "surname").as[String]
      val middle_name = (json \ "middle_name").as[String]
      val code = (json \ "code").as[String]
      val position = (json \ "positions" \ "title").as[String]
      val email = (json \ "email").as[String]

      val staff = new Staffer(name, image, birth, surname, middle_name, code,
        Positions.findByTitle(position).get, email)
      Db.save[Staffer](staff)

      Ok(Json.obj("status" -> "success", "message" -> "Staff added"))
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def editStaff(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val name = (json \ "name").as[String]
      val image = (json \ "image").as[String]
      val birth = (json \ "birth").as[Long]
      val surname = (json \ "surname").as[String]
      val middle_name = (json \ "middle_name").as[String]
      val code = (json \ "code").as[String]
      val position = (json \ "positions" \ "title").as[String]
      val email = (json \ "email").as[String]

      Logger.debug("positions: " + position)

      Staffer.findByQrCode(code).map {
        staffer => {
          Staffer.updateStaffer(staffer, name, image, birth, surname, middle_name,
            Positions.findByTitle(position).get, email)
          Ok(Json.obj("status" -> "success", "message" -> "Staff info changed"))
        }
      }.getOrElse{
        BadRequest(Json.obj("status" -> "fail", "message" -> "Staff not found"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def getAllHistory(login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login, password).map {
      _ => {
        val histories = Db.query[StaffHistory].order("id", reverse = true).fetch()
        Ok(Json.toJson(histories))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Администратор не найден"))
    }
  }
}
