package controllers

import com.google.inject.Inject
import models.{Tariff, TariffOption}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class TariffOptionsController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.TariffOptionsController.tariffOptionsList(0, 2, ""))

  val tariffsOptionsForm: Form[TariffOption] = Form(
    mapping(
      "keyword" -> text,
      "title" -> text
    )(TariffOption.apply)(TariffOption.unapply)
  )

  def tariffOptionsList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tariffOptions(
      TariffOption.tariffOptionsList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormTariffOptions(tariffsOptionsForm))
  }

  def tariffOptionsForm(): Action[AnyContent] = Action { implicit request =>
    tariffsOptionsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormTariffOptions(formWithErrors)),
      tariffOption => {
        TariffOption.save(tariffOption)
        Home.flashing("success" -> "Tariff option %s has been created".format(tariffOption.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    TariffOption.findById(id).map { tariffOption =>
      TariffOption.delete(tariffOption)
      Home.flashing("success" -> "Tariff option has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Tariff option not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    TariffOption.findById(id).map { tariff =>
      Ok(views.html.editFormTariffOptions(id, tariffsOptionsForm.fill(tariff)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Tariff option not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    tariffsOptionsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormTariffOptions(id, formWithErrors)),
      tariffOption => {
        TariffOption.update(id, tariffOption)
        Home.flashing("success" -> "Tariff option %s has been updated".format(tariffOption.title))
      }
    )
  }

  def getTariffOptions: Action[AnyContent] = Action {
    val tariffOptions = TariffOption.getTariffOptions()

    val tariffOptionsList = tariffOptions.map {
      tariffOption => {
        Json.obj(
          "id" -> tariffOption.id,
          "keyword" -> tariffOption.keyword,
          "title" -> tariffOption.title
        )
      }
    }
    Ok(Json.toJson(tariffOptionsList))
  }
}
