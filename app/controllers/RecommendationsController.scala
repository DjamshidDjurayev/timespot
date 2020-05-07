package controllers

import java.text.DateFormat
import java.util.Date

import com.google.inject.Inject
import models.Recommendation
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class RecommendationsController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.RecommendationsController.recommendationsList(0, 2, ""))

  val recommendationsForm: Form[Recommendation] = Form(
    mapping(
      "title" -> text,
      "startDate" -> of(dateFormat),
      "endDate" -> of(dateFormat),
      "status" -> text,
      "profType" -> text
    )( (title, startDate, endDate, status, profType) => Recommendation(title, startDate.getTime, endDate.getTime, System.currentTimeMillis(), status, profType))
    ((recommendation: Recommendation) => Some(recommendation.title, new Date(recommendation.startDate), new Date(recommendation.endDate), recommendation.status, recommendation.profType))
  )

  def recommendationsList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.recommendations(
      Recommendation.recommendationsList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormRecommendations(recommendationsForm))
  }

  def recForm(): Action[AnyContent] = Action { implicit request =>
    recommendationsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormRecommendations(formWithErrors)),
      recommendation => {
        Recommendation.save(recommendation)
        Home.flashing("success" -> "Recommendation option %s has been created".format(recommendation.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Recommendation.findById(id).map { recommendation =>
      Recommendation.delete(recommendation)
      Home.flashing("success" -> "Recommendation option has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Recommendation option not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Recommendation.findById(id).map { recommendation =>
      Ok(views.html.editFormRecommendations(id, recommendationsForm.fill(recommendation)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Recommendation option not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    recommendationsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormRecommendations(id, formWithErrors)),
      recommendation => {
        Recommendation.update(id, recommendation)
        Home.flashing("success" -> "Recommendation option %s has been updated".format(recommendation.title))
      }
    )
  }

  def getRecommendations: Action[AnyContent] = Action {
    val recommendations = Recommendation.getRecommendations()

    val recommendationsList = recommendations.map {
      recommendation => {
        Json.obj(
          "id" -> recommendation.id,
          "title" -> recommendation.title,
          "startDate" -> recommendation.startDate,
          "endDate" -> recommendation.endDate,
          "creationDate" -> recommendation.creationDate,
          "status" -> recommendation.status,
          "profType" -> recommendation.profType
        )
      }
    }

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> recommendationsList
    )

    Ok(Json.toJson(response))
  }

  def getDocRecommendations: Action[AnyContent] = Action {
    val list = Recommendation.getRecommendationsByProf("doctor")

    val docList = list.map {
      recommendation => {
        Json.obj(
          "id" -> recommendation.id,
          "title" -> recommendation.title,
          "startDate" -> recommendation.startDate,
          "endDate" -> recommendation.endDate,
          "creationDate" -> recommendation.creationDate,
          "status" -> recommendation.status,
          "profType" -> recommendation.profType
        )
      }
    }

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> docList
    )

    Ok(Json.toJson(response))
  }

  def getLawyerRecommendations: Action[AnyContent] = Action {
    val list = Recommendation.getRecommendationsByProf("lawyer")

    val lawList = list.map {
      recommendation => {
        Json.obj(
          "id" -> recommendation.id,
          "title" -> recommendation.title,
          "startDate" -> recommendation.startDate,
          "endDate" -> recommendation.endDate,
          "creationDate" -> recommendation.creationDate,
          "status" -> recommendation.status,
          "profType" -> recommendation.profType
        )
      }
    }

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> lawList
    )

    Ok(Json.toJson(response))
  }
}
