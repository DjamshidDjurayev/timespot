package controllers

import com.google.inject.Inject
import models.{Alert}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.ExecutionContext

class Notifications @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.Notifications.list4(0, 2, ""))

  val alertsForm: Form[Alert] = Form(
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "notificationType" -> text,
      "action" -> text
    )( (title, description, notificationType, action) => Alert(title, description, System.currentTimeMillis(), notificationType, action))( (alert: Alert) => Some(alert.title, alert.description, alert.notificationType, alert.action))
  )

  def list4(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.alerts(
      Alert.list4(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormNotifications(alertsForm))
  }

  def addNotification(): Action[AnyContent] = Action { implicit request =>
    alertsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormNotifications(formWithErrors)),
      alert => {
        Alert.save(alert)
        Home.flashing("success" -> "Notification %s has been created".format(alert.title))
      }
    )
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Alert.findById(id).map { alert =>
      Ok(views.html.editFormNotifications(id, alertsForm.fill(alert)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Alert not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    alertsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormNotifications(id, formWithErrors)),
      alert => {
        Alert.update(id, alert)
        Home.flashing("success" -> "Notification %s has been updated".format(alert.title))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Alert.findById(id).map { alert =>
      Alert.delete(alert)
      Home.flashing("success" -> "Notification has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Notification not found")
    }
  }

  def getNotifications: Action[AnyContent] = Action {
    val notifications = Alert.getNotifications()

    val notificationList = notifications.map {
      notification => {
        Json.obj(
          "id" -> notification.id,
          "title" -> notification.title,
          "description" -> notification.description,
          "date" -> notification.date,
          "notificationType" -> notification.notificationType,
          "action" -> notification.action
        )
      }
    }
    Ok(Json.toJson(notificationList))
  }

  def getAlerts: Action[AnyContent] = Action {
    val alerts = Alert.getAlerts()

    val alertList = alerts.map {
      alert => {
        Json.obj(
          "id" -> alert.id,
          "title" -> alert.title,
          "description" -> alert.description,
          "date" -> alert.date,
          "notificationType" -> alert.notificationType,
          "action" -> alert.action
        )
      }
    }
    Ok(Json.toJson(alertList))
  }

  def getOffers: Action[AnyContent] = Action {
    val offers = Alert.getOffers()

    val offerList = offers.map {
      offer => {
        Json.obj(
          "id" -> offer.id,
          "title" -> offer.title,
          "description" -> offer.description,
          "date" -> offer.date,
          "notificationType" -> offer.notificationType,
          "action" -> offer.action
        )
      }
    }
    Ok(Json.toJson(offerList))
  }
}
