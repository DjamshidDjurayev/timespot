package controllers

import java.text.DateFormat
import javax.swing.text.DateFormatter

import com.sun.jmx.snmp.Timestamp
import models.{History, DBase, Staffer}
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat, DateTimeFormatter}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import java.util.Date
import play.api.data.format.Formats._
import sun.security.timestamp.Timestamper

import views.html

class Staff extends Controller {

  val Home = Redirect(routes.Staff.list(0, 2, ""))


  val stafferForm: Form[Staffer] = Form(
    mapping(
      "name" -> text,
      "surname" -> text,
      "middle_name" -> text,
      "position" -> text,
      "image" -> text,
      "birth" -> jodaDate("yyyy-MM-dd"),
      "code" -> nonEmptyText
    )(Staffer.apply)(Staffer.unapply))

  def addStaff = Action { implicit request =>
    stafferForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.createForm(formWithErrors)),
      staff => {
        DBase.save[Staffer](staff)
        Home.flashing("success" -> "User %s has been created".format(staff.name))
      }
    )
  }

  def getStaff = Action {
    val staffs = DBase.query[Staffer].fetch()
    Ok(Json.toJson(staffs))
  }

  def getOneStaff(id: Long) = Action {
    Ok(Json.toJson(Staffer.findById(id)))
  }

  def getStaffByQrCode(code: String) = Action {
    Ok(Json.toJson(Staffer.findByQrCode(code)))
  }

  def create = Action {
    Ok(views.html.createForm(stafferForm))
  }

  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(views.html.staff(
      Staffer.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  def update(id: Long) = Action { implicit request =>
    stafferForm.bindFromRequest.fold(
    formWithErrors => BadRequest(views.html.editForm(id, formWithErrors)),
    staff => {
      Staffer.update(id, staff)
      Home.flashing("success" -> "User %s has been updated".format(staff.name))
    }
    )
  }

  def edit(id: Long) = Action { implicit request =>
    Staffer.findById(id).map { staff =>
      Ok(views.html.editForm(id, stafferForm.fill(staff)))
    }.getOrElse(NotFound)
  }

  def delete(id: Long) = Action {
    Staffer.delete(id)
    Home.flashing("success" -> "User has been deleted")
  }



  def history(userId: Long, income: Long, outcome: Long) = Action {
    val ii: DateTime = DateTime.now()
    ii.withMillis(income).toDateTime
    val jj: DateTime = DateTime.now()
    jj.withMillis(outcome).toDateTime

    val user = Staffer.findById(userId)
    val history = History(ii, jj, user.get)

    DBase.save[History](history)
    Ok("good")

  }

}
