package controllers

import java.time.{LocalDateTime, LocalTime}
import java.util.Date

import com.google.inject.Inject
import models.{Appointment, TariffOption}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AppointmentsController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.AppointmentsController.appointmentsList(0, 2, ""))

  val appointmentsForm: Form[Appointment] = Form(
    mapping(
      "startTime" -> nonEmptyText,
      "endTime" -> nonEmptyText,
      "createdAt" -> of(dateFormat)
    )((startTime, endTime, createdAt) => Appointment(startTime, endTime, createdAt.getTime))
    ( (appointment: Appointment) => Some(appointment.startTime, appointment.endTime, new Date(appointment.createdAt)))
  )

  def appointmentsList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.appointments(
      Appointment.appointmentsList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormAppointment(appointmentsForm))
  }

  def appForm(): Action[AnyContent] = Action { implicit request =>
    appointmentsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormAppointment(formWithErrors)),
      appointment => {
        Appointment.save(appointment)
        Home.flashing("success" -> "Appointment has been created")
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Appointment.findById(id).map { appointment =>
      Appointment.delete(appointment)
      Home.flashing("success" -> "Appointment has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Appointment not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Appointment.findById(id).map { appointment =>
      Ok(views.html.editFormAppointments(id, appointmentsForm.fill(appointment)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Appointment not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    appointmentsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormAppointments(id, formWithErrors)),
      appointment => {
        Appointment.update(id, appointment)
        Home.flashing("success" -> "Appointment has been updated")
      }
    )
  }

  def getAppointments(date: Long): Action[AnyContent] = Action {
    val appointments = Appointment.getAppointmentsByDate(date)

    val appointmentsList = appointments.map {
      appointment => {
        Json.obj(
          "id" -> appointment.id,
          "startTime" -> appointment.startTime,
          "endTime" -> appointment.endTime,
          "createdAt" -> appointment.createdAt
        )
      }
    }

    val response = Json.obj(
      "code" -> 200,
      "status" -> "success",
      "data" -> appointmentsList
    )

    Ok(Json.toJson(response))
  }
}
