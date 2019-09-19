package controllers

import java.io.File

import com.google.inject.Inject
import models._
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc._

class Administrator @Inject()(components: ControllerComponents) extends AbstractController(components) {
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
    val positions = Db.query[Positions].order("id", reverse = true).fetch()
    Ok(Json.toJson(positions))
  }

  def getAdmins: Action[AnyContent] = Action {
    Ok(Json.toJson(Db.query[Admin].fetch()))
  }

  def addPosition(title: String): Action[AnyContent] = Action {
    Positions.findByTitle(title).map {
      _ => {
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
      _ => {
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
      _ => {
        Ok(Json.obj("status" -> "fail", "message" -> "Уже существует"))
      }
    }.getOrElse {
      val admin = new Admin(name, surname, login, password)
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

  def getStatistics(login: String, password: String): Action[AnyContent] = Action {
    Admin.findAdmin(login, password).map {
      _ => {
        val staffCounter = Staffer.staffCount()
        val historyCounter = History.historyGeneralCount()
        val newsCounter = PaperNew.newsCount()
        val adminCounter = Admin.adminCount()
        val positionCounter = Positions.positionCount()

        Ok(Json.obj(
          "staff_count" -> staffCounter,
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
