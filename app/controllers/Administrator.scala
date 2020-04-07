package controllers

import java.io.File

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.Files
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.JwtUtility

import scala.concurrent.ExecutionContext

class Administrator @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.Administrator.list3(0, 2, ""))

  val adminsForm: Form[Admin] = Form(
    mapping(
      "name" -> text,
      "surname" -> text,
      "login" -> nonEmptyText,
      "password" -> nonEmptyText,
      "middleName" -> text,
      "phone" -> text,
      "passport" -> text
    )((name, surname, login, password, middleName, phone, passport) =>
      Admin(name, surname, login, password, JwtUtility.createToken(login + password + System.currentTimeMillis()), middleName, phone, passport))
    ((arg: Admin) => Some(arg.name, arg.surname, arg.login, arg.password, arg.middleName, arg.phone, arg.passport))
  )

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    adminsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormAdmins(id, formWithErrors)),
      admin => {
        Admin.update(id, admin)
        Home.flashing("success" -> "Admin %s has been updated".format(admin.login))
      }
    )
  }

  def adminForm(): Action[AnyContent] = Action { implicit request =>
    adminsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormaAdmins(formWithErrors)),
      admin => {
        Admin.save(admin)
        Home.flashing("success" -> "Admin %s has been created".format(admin.login))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Admin.findById(id).map { admin =>
      Admin.delete(admin)
      Home.flashing("success" -> "Admin has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Admin not found")
    }
  }

  def list3(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.admins(
      Admin.list3(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormaAdmins(adminsForm))
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Admin.findById(id).map { admin =>
      Ok(views.html.editFormAdmins(id, adminsForm.fill(admin)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Admin not found"))
    }
  }

  def getPositions: Action[AnyContent] = Action {
    val positions = Db.query[Positions].order("id", reverse = true).fetch()
    Ok(Json.toJson(positions))
  }

  def getAdmins: Action[AnyContent] = Action {
    val admins = Admin.getAdmins()

    val list = admins.map {
      admin => {
        Json.obj(
          "id" -> admin.id,
          "name" -> admin.name,
          "surname" -> admin.surname,
          "login" -> admin.login,
          "password" -> admin.password,
          "token" -> admin.token,
          "middleName" -> admin.middleName,
          "phone" -> admin.phone,
          "passport" -> admin.passport
        )
      }
    }
    Ok(Json.toJson(list))
  }

  def login(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val login = (json \ "login").as[String]
      val password = (json \ "password").as[String]

      Admin.findAdmin(login, password).map {
        v => {
          Admin.updateToken(v).map {
            admin => {
              Ok(Json.toJson(
                Json.obj(
                  "status" -> "success",
                  "code" -> 200,
                  "token" -> admin.token,
                  "account" -> Json.obj(
                    "id" -> admin.id,
                    "name" -> admin.name,
                    "surname" -> admin.surname,
                    "login" -> admin.login,
                    "password" -> admin.password,
                    "middleName" -> admin.middleName,
                    "phone" -> admin.phone,
                    "passport" -> admin.passport
                  )
                )
              ))
            }
          }.getOrElse {
            NotFound(Json.obj("status" -> "fail", "message" -> "Error"))
          }
        }
      }.getOrElse {
        NotFound(Json.obj("status" -> "fail", "message" -> "User not found"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def updateUser(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val id = (json \ "id").as[Long]
      val name = (json \ "name").as[String]
      val surname = (json \ "surname").as[String]
      val login = (json \ "login").as[String]
      val middleName = (json \ "middleName").as[String]
      val phone = (json \ "phone").as[String]
      val passport = (json \ "passport").as[String]

      Admin.findById(id).map {
        v => {
          val updatedAdmin = Admin.updateAdmin(v, name, surname, login, middleName, phone, passport)
          Ok(Json.toJson(
            Json.obj(
              "status" -> "success",
              "code" -> 200,
              "token" -> updatedAdmin.token
            )
          ))
        }
      }.getOrElse {
        NotFound(Json.obj("status" -> "fail", "message" -> "User not found"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def logout(): Action[AnyContent] = Action { implicit request =>
    val requestHeader = request.headers.get("jwt_token")

    requestHeader.map { token => {
      Admin.findAdminByToken(token).map {
        _ => {
          Ok(Json.toJson(
            Json.obj(
              "status" -> "success",
              "code" -> 200
            )
          ))
        }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "not authorized"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
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
      //      val admin = new Admin(name, surname, login, password, "")
      //      Admin.save(admin)
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
        val historyCounter = StaffHistory.historyGeneralCount()
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
