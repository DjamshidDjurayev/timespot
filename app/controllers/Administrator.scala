package controllers

import java.io.File

import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

class Administrator extends Controller {

  def adminAuth(login: String, password: String) = Action {

    Admin.findAdmin(login, password).map {
      admin => {
        Ok(Json.obj("status" -> "success", "id" -> admin.id))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Administrator not found"))
    }
  }

  def getPositions = Action {
    val positions = Db.query[Positions].fetch()
    Ok(Json.toJson(positions))
  }

  def addPosition(title: String) = Action {
    val position = new Positions(title)
    val result = Db.save[Positions](position)
    Ok("success")
  }

  def sendPicture = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(new File(s"/public/images/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      BadRequest("error")
    }
  }

//  def sendPicture = Action(parse.temporaryFile) { request =>
//    request.body.moveTo(new File("public/images"))
//    Ok("File uploaded")
//  }

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

}
