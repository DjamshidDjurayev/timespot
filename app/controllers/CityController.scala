package controllers

import java.util.Date

import com.google.inject.Inject
import models.{City}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats.dateFormat
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class CityController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.CityController.cityList(0, 2, ""))

  val citiesForm: Form[City] = Form(
    mapping(
      "title" -> text,
      "creationDate" -> of(dateFormat)
    )( (title, creationDate) => City(title, creationDate.getTime))( (city: City) => Some(city.title, new Date(city.creation_date)))
  )

  def cityList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.cities(
      City.cityList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormCities(citiesForm))
  }

  def cityForm(): Action[AnyContent] = Action { implicit request =>
    citiesForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormCities(formWithErrors)),
      city => {
        City.save(city)
        Home.flashing("success" -> "City %s has been created".format(city.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    City.findById(id).map { city =>
      City.delete(city)
      Home.flashing("success" -> "City has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "City not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    City.findById(id).map { city =>
      Ok(views.html.editFormCities(id, citiesForm.fill(city)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "City not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    citiesForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormCities(id, formWithErrors)),
      city => {
        City.update(id, city)
        Home.flashing("success" -> "City %s has been updated".format(city.title))
      }
    )
  }

  def getCities: Action[AnyContent] = Action {
    val cities = City.getCities

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> cities
    )

    Ok(Json.toJson(response))
  }
}
