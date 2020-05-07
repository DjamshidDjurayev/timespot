package controllers

import com.google.inject.Inject
import models.{Assigner, Tariff}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AssignersController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.AssignersController.assignerList(0, 2, ""))

  val assignersForm: Form[Assigner] = Form(
    mapping(
      "name" -> text,
      "surname" -> text,
      "middleName" -> text,
      "phone" -> text,
      "avatar" -> text,
      "speciality" -> text,
      "city" -> text
    )(Assigner.apply)(Assigner.unapply)
  )

  def assignerList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.assigners(
      Assigner.assignersList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormAssigners(assignersForm))
  }

  def assignerForm(): Action[AnyContent] = Action { implicit request =>
    assignersForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormAssigners(formWithErrors)),
      assigner => {
        Assigner.save(assigner)
        Home.flashing("success" -> "Assigner %s has been created".format(assigner.name))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Assigner.findById(id).map { assigner =>
      Assigner.delete(assigner)
      Home.flashing("success" -> "Assigner has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Assigner not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Assigner.findById(id).map { tariff =>
      Ok(views.html.editFormAssigners(id, assignersForm.fill(tariff)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Assigner not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    assignersForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormAssigners(id, formWithErrors)),
      assigner => {
        Assigner.update(id, assigner)
        Home.flashing("success" -> "Assigner %s has been updated".format(assigner.name))
      }
    )
  }

  def getAssigners: Action[AnyContent] = Action {
    val assigners = Assigner.getAssigners()

    val assignersList = assigners.map {
      assigner => {
        Json.obj(
          "id" -> assigner.id,
          "name" -> assigner.name,
          "surname" -> assigner.surname,
          "middleName" -> assigner.middleName,
          "phone" -> assigner.phone,
          "avatar" -> assigner.avatar,
          "speciality" -> assigner.speciality,
          "city" -> assigner.city
        )
      }
    }

    val assignerResponse = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> assigners
    )

    Ok(Json.toJson(assignerResponse))
  }
}
