package controllers



import models.{History, DBase, Staffer}
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class Staff extends Controller {


  val Home = Redirect(routes.Staff.list(0, 2, ""))

  val stafferForm: Form[Staffer] = Form(
    mapping(
      "name" -> text,
      "image" -> text,
      "birth" -> jodaDate("yyyy-MM-dd"),
      "surname" -> text,
      "middle_name" -> text,
      "code" -> nonEmptyText,
      "position" -> text
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
//    staffs.
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

  def addHistory(code: String, action: Int) = Action {

    val actionDateTime = DateTime.now() // TODO check

    Staffer.findByQrCode(code).map {
      staff => {
        val history  = History(staff, action, actionDateTime, actionDateTime.toLocalDate)
        DBase.save[History](history)
        Ok(Json.obj("status" -> "success", "message" -> "success"))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "not found"))
    }

  }


  def getHistory(code: String) = Action {
    Staffer.findByQrCode(code).map {
      history => {
        Ok(Json.toJson(History.findById(history)))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "not found"))
    }
  }

  def getActionsByDate(userId: String, date: Long) = Action {

    Staffer.findByQrCode(userId).map {
      staff => {
        Ok(Json.toJson(History.getStaffActionsByDate(staff, date)))
      }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "not found"))
    }

  }

  // handling POST request  from client side
  def staffRegistration() = Action { implicit request =>

    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    // Expecting json body
    jsonBody.map { json => {
      val name = (json \ "name").as[String]
      val image = (json \ "image").as[String]
      val birth = (json \ "birth").as[Long]
      val surname = (json \ "surname").as[String]
      val middle_name = (json \ "middle_name").as[String]
      val code = (json \ "code").as[String]
      val position = (json \ "position").as[String]
//      Ok(Json.obj())
      val staff = new Staffer(name, image, new DateTime(birth), surname, middle_name, code, position)
      Ok(Json.toJson(staff))
    }
    }.getOrElse {
      BadRequest("Expecting application/json request body")
    }
    }



}
