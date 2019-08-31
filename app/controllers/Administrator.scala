package controllers

import java.io.File
import java.util.concurrent.Future

import models._
import play.api.libs.Files
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

class Administrator extends Controller {
  def adminAuth(login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login, password).map {
      admin => {
        Ok(Json.obj("status" -> "success", "id" -> admin.id))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Администратор не найден"))
    }
  }

  def getPositions: Action[AnyContent] = Action {
    val positions = Db.query[Positions].order("id", true).fetch()
    Ok(Json.toJson(positions))
  }

  def getAdmins: Action[AnyContent] = Action {
    Ok(Json.toJson(Db.query[Admin].fetch()))
  }

  def addPosition(title: String): Action[AnyContent] = Action {
    Positions.findByTitle(title).map {
      position => {
        Ok(Json.obj("status" -> "fail", "message" -> "Уже существует"))
      }
    }.getOrElse {
      val position = new Positions(title)
      Db.save[Positions](position)
      Ok(Json.obj("status" -> "success", "message" -> "Должность добавлена"))
    }
  }

  def deletePosition(title: String): Action[AnyContent] = Action {
    Positions.findByTitle(title).map {
      position => {
        Positions.delete(position)
        Ok(Json.obj("status" -> "success", "message" -> "Должность удалена"))
      }
    }.getOrElse {
      Ok(Json.obj("status" -> "fail", "message" -> "Должность не найдена"))
    }
  }

  def editPosition(oldTitle: String, newTitle: String): Action[AnyContent] = Action {
    Positions.findByTitle(newTitle).map {
      position => {
        Ok(Json.obj("status" -> "fail", "message" -> "Уже существует"))
      }
    }.getOrElse {
      val position = Positions.findByTitle(oldTitle)
      Positions.update(position.get, newTitle)
      Ok(Json.obj("status" -> "success", "message" -> "Должность изменена"))
    }
  }

  def sendPicture: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      val filename = picture.filename
      picture.ref.moveTo(new File(s"/assets/public/images/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      BadRequest("error")
    }
  }

  def addAdmin(name: String, surname: String, login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login: String, password: String).map {
      admin => {
        Ok(Json.obj("status" -> "fail", "message" -> "Уже существует"))
      }
    }.getOrElse {
      val admin = new Admin(name,surname,login,password)
      Admin.save(admin)
      Ok(Json.obj("status" -> "success", "message" -> "Администратор добавлен"))
    }
  }

  def deleteAdmin(login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login, password).map {
      admin => {
        Admin.delete(admin)
        Ok(Json.obj("status" -> "success", "message" -> "Администратор удален"))
      }
    }.getOrElse {
      Ok(Json.obj("status" -> "success", "message" -> "Не существует"))
    }
  }

//  def sendPicture = Action(parse.temporaryFile) { request =>
//    request.body.moveTo(new File("public/images"))
//    Ok("File uploaded")
//  }ф

//  def staffRegistration() = Action { implicit request =>
//
//    val body: AnyContent = request.body
//    val jsonBody: Option[JsValue] = body.asJson
//
//    // Expecting json body
//    jsonBody.map { json => {
//      val name = (json \ "name").as[String]
//      val image = (json \ "image").as[String]
//      val birth = (json \ "birth").as[Long]
//      val surname = (json \ "surname").as[String]
//      val middle_name = (json \ "middle_name").as[String]
//      val code = (json \ "code").as[String]
//      val position = (json \ "position").as[String]
//      //      Ok(Json.obj())
//      val staff = new Staffer(name, image, new DateTime(birth), surname, middle_name, code, position)
//      Ok(Json.toJson(staff))
//    }
//    }.getOrElse {
//      BadRequest("Expecting application/json request body")
//    }
//  }

  def getStatistics(login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login, password).map {
      admin => {
        val staffCounter = Staffer.staffCount()
        val historyCounter = History.historyGeneralCount()
        val newsCounter = PaperNew.newsCount()
        val adminCounter = Admin.adminCount()
        val positionCounter = Positions.positionCount()
        Ok(Json.obj("staff_count" -> staffCounter,
          "history_count" -> historyCounter,
        "news_count" -> newsCounter,
        "admin_count" -> adminCounter,
        "position_count" -> positionCounter))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Администратор не найден"))
    }
  }

}
