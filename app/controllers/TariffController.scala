package controllers

import com.google.inject.Inject
import models.{Assigner, Tariff, TariffOption}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.Forms.mapping
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.ExecutionContext

class TariffController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.TariffController.tariffList(0, 2, ""))

  val tariffsForm: Form[Tariff] = Form(
    mapping(
      "title" -> text,
      "city" -> text,
      "price" -> of(doubleFormat),
      "description" -> text
    )( (title, city, price, description) => Tariff(title, city, price, description, System.currentTimeMillis()))
    ( (tariff: Tariff) => Some(tariff.title, tariff.city, tariff.price, tariff.description))
  )

  def tariffList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tariffs(
      Tariff.tariffList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormTariffs(tariffsForm))
  }

  def tariffForm(): Action[AnyContent] = Action { implicit request =>
    tariffsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormTariffs(formWithErrors)),
      tariff => {
        Tariff.save(tariff)
        Home.flashing("success" -> "Tariff %s has been created".format(tariff.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Tariff.findById(id).map { tariff =>
      Tariff.delete(tariff)
      Home.flashing("success" -> "Tariff has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Tariff not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Tariff.findById(id).map { tariff =>
      Ok(views.html.editFormTariffs(id, tariffsForm.fill(tariff)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Tariff not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    tariffsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormTariffs(id, formWithErrors)),
      tariff => {
        Tariff.update(id, tariff)
        Home.flashing("success" -> "Tariff %s has been updated".format(tariff.title))
      }
    )
  }

  def getTariffs: Action[AnyContent] = Action {
    val tariffs = Tariff.getTariffs()
    val options = TariffOption.getTariffOptions()
    val assigners = Assigner.getAssigners()

    val tariffList = tariffs.map {
      tariff => {
        Json.obj(
          "id" -> tariff.id,
          "title" -> tariff.title,
          "city" -> tariff.city,
          "price" -> tariff.price,
          "description" -> tariff.description,
          "creation_date" -> tariff.creation_date,
          "options" -> options.map {
            option => {
              Json.obj(
                "id" -> option.id,
                "keyword" -> option.keyword,
                "title" -> option.title
              )
            }
          },
          "assigners" -> assigners.map {
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
        )
      }
    }
    Ok(Json.toJson(tariffList))
  }
}
