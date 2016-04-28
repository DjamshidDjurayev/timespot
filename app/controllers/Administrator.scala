package controllers

import models.{DBase, Admin, History, Staffer}
import play.api.libs.json.Json
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

}
