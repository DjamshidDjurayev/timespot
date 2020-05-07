package controllers

import java.util.Date

import com.google.inject.Inject
import models.{MedHistory, Recommendation}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class MedHistoryController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.MedHistoryController.medHistoryList(0, 2, ""))

  val medHistoryForm: Form[MedHistory] = Form(
    mapping(
      "title" -> text,
      "description" -> text,
      "date" -> of(dateFormat),
      "fileName" -> text,
      "fileFormat" -> text,
      "historyType" -> nonEmptyText
    )( (title, description, date, fileName, fileFormat, historyType) => MedHistory(title, description, date.getTime, fileName, fileFormat, historyType))
    ((medHistory: MedHistory) => Some(medHistory.title, medHistory.description, new Date(medHistory.date), medHistory.fileName, medHistory.fileFormat, medHistory.historyType))
  )

  def medHistoryList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.medHistory(
      MedHistory.medHistoryList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormMedHistory(medHistoryForm))
  }

  def historyForm(): Action[AnyContent] = Action { implicit request =>
    medHistoryForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormMedHistory(formWithErrors)),
      history => {
        MedHistory.save(history)
        Home.flashing("success" -> "MedHistory %s has been created".format(history.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    MedHistory.findById(id).map { history =>
      MedHistory.delete(history)
      Home.flashing("success" -> "MedHistory has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "MedHistory not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    MedHistory.findById(id).map { history =>
      Ok(views.html.editFormMedHistory(id, medHistoryForm.fill(history)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "MedHistory option not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    medHistoryForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormMedHistory(id, formWithErrors)),
      history => {
        MedHistory.update(id, history)
        Home.flashing("success" -> "MedHistory option %s has been updated".format(history.title))
      }
    )
  }

  def getMedHistory: Action[AnyContent] = Action {
    val medHistory = MedHistory.getMedHistoryList()

    val medHistoryList = medHistory.map {
      history => {
        Json.obj(
          "id" -> history.id,
          "title" -> history.title,
          "description" -> history.description,
          "date" -> history.date,
          "fileName" -> history.fileName,
          "fileFormat" -> history.fileFormat,
          "historyType" -> history.historyType
        )
      }
    }
    Ok(Json.toJson(medHistoryList))
  }

  def getConclusions: Action[AnyContent] = Action {
    val list = MedHistory.getMedHistoryByType("conclusion")

    val conclusionList = list.map {
      history => {
        Json.obj(
          "id" -> history.id,
          "title" -> history.title,
          "description" -> history.description,
          "date" -> history.date,
          "fileName" -> history.fileName,
          "fileFormat" -> history.fileFormat,
          "historyType" -> history.historyType
        )
      }
    }

    val conclusionsResponse = Json.obj(
      "code" -> 200,
      "status" -> "message",
      "data" -> conclusionList
    )

    Ok(Json.toJson(conclusionsResponse))
  }

  def getDirections: Action[AnyContent] = Action {
    val list = MedHistory.getMedHistoryByType("direction")

    val directionList = list.map {
      history => {
        Json.obj(
          "id" -> history.id,
          "title" -> history.title,
          "description" -> history.description,
          "date" -> history.date,
          "fileName" -> history.fileName,
          "fileFormat" -> history.fileFormat,
          "historyType" -> history.historyType
        )
      }
    }

    val directionsResponse = Json.obj(
      "code" -> 200,
      "status" -> "message",
      "data" -> directionList
    )

    Ok(Json.toJson(directionsResponse))
  }

  def getActs: Action[AnyContent] = Action {
    val acts = MedHistory.getMedHistoryByType("act")

    val actsList = acts.map {
      history => {
        Json.obj(
          "id" -> history.id,
          "title" -> history.title,
          "description" -> history.description,
          "date" -> history.date,
          "fileName" -> history.fileName,
          "fileFormat" -> history.fileFormat
        )
      }
    }

    val actsResponse = Json.obj(
      "code" -> 200,
      "status" -> "message",
      "data" -> actsList
    )

    Ok(Json.toJson(actsResponse))
  }
}
