package controllers

import java.util.Date

import com.google.inject.Inject
import models.PaymentSchedule
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats._
import play.api.data.format.Formats.dateFormat
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class PaymentScheduleController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.PaymentScheduleController.schedulesList(0, 2, ""))

  val sForm: Form[PaymentSchedule] = Form(
    mapping(
      "title" -> nonEmptyText,
      "date" -> of(dateFormat),
      "amount" -> of(doubleFormat),
      "currency" -> nonEmptyText,
      "action" -> nonEmptyText
    )((title, date, amount, currency, action) => PaymentSchedule(title, date.getTime, amount, currency, action))
    ((schedule: PaymentSchedule) => Some(schedule.title, new Date(schedule.date), schedule.amount, schedule.currency, schedule.actionType))
  )

  def schedulesList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.schedules(
      PaymentSchedule.scheduleList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormSchedules(sForm))
  }

  def schedulesForm(): Action[AnyContent] = Action { implicit request =>
    sForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormSchedules(formWithErrors)),
      schedule => {
        PaymentSchedule.save(schedule)
        Home.flashing("success" -> "PaymentSchedule %s has been created".format(schedule.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    PaymentSchedule.findById(id).map { schedule =>
      PaymentSchedule.delete(schedule)
      Home.flashing("success" -> "PaymentSchedule has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "PaymentSchedule not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    PaymentSchedule.findById(id).map { schedule =>
      Ok(views.html.editFormSchedules(id, sForm.fill(schedule)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "PaymentSchedule not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    sForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormSchedules(id, formWithErrors)),
      schedule => {
        PaymentSchedule.update(id, schedule)
        Home.flashing("success" -> "PaymentSchedule %s has been updated".format(schedule.title))
      }
    )
  }

  def getPaymentSchedules: Action[AnyContent] = Action {
    val schedules = PaymentSchedule.getPaymentSchedules

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> schedules
    )

    Ok(Json.toJson(response))
  }
}
