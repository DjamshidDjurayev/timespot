package controllers

import java.util.Date

import com.google.inject.Inject
import models.{Admin, Payment}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class PaymentsController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.PaymentsController.paymentsList(0, 2, ""))

  val form: Form[Payment] = Form(
    mapping(
      "date" -> of(dateFormat),
      "paymentMethod" -> nonEmptyText,
      "amount" -> of(doubleFormat),
      "currency" -> nonEmptyText
    )((date, paymentMethod, amount, currency) => Payment(date.getTime, paymentMethod, amount, currency))
    ((payment: Payment) => Some(new Date(payment.date), payment.paymentMethod, payment.amount, payment.currency))
  )

  def paymentsList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.payments(
      Payment.paymentList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormPayments(form))
  }

  def paymentsForm(): Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormPayments(formWithErrors)),
      payment => {
        Payment.save(payment)
        Home.flashing("success" -> "Payment %s has been created".format(payment.paymentMethod))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Payment.findById(id).map { payment =>
      Payment.delete(payment)
      Home.flashing("success" -> "Payment has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Payment not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Payment.findById(id).map { payment =>
      Ok(views.html.editFormPayments(id, form.fill(payment)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Payment not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormPayments(id, formWithErrors)),
      payment => {
        Payment.update(id, payment)
        Home.flashing("success" -> "Payment %s has been updated".format(payment.paymentMethod))
      }
    )
  }

  def getPayments: Action[AnyContent] = Action { implicit request =>
    val requestHeader = request.headers.get("jwt_token")

    requestHeader.map { token => {
      Admin.findAdminByToken(token).map {
        _ => {
          val payments = Payment.getPayments()

          val paymentsList = payments.map {
            payment => {
              Json.obj(
                "id" -> payment.id,
                "date" -> payment.date,
                "paymentMethod" -> payment.paymentMethod,
                "amount" -> payment.amount,
                "currency" -> payment.currency
              )
            }
          }
          Ok(Json.toJson(paymentsList))
        }
      }.getOrElse {
        Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
      }
    }
    }.getOrElse {
      Unauthorized(Json.obj("status" -> 401, "message" -> "Not authorized"))
    }
  }
}