package controllers

import com.google.inject.Inject
import common.cloudinary.CloudinaryProvider
import common.mqtt.MqttServiceProvider
import common.fcm.FcmProvider
import models.{Device, PaperNew}
import org.joda.time.DateTime
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.Files
import play.api.libs.json.{JsObject, Json}
import service.model.MQTTPayload

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}
import scala.util.{Failure, Success}

class News @Inject()(fcmProvider: FcmProvider,
                     cloudinaryProvider: CloudinaryProvider,
                     mqttServiceProvider: MqttServiceProvider)  extends Controller {

  val Home: Result = Redirect(routes.News.list2(0, 2, ""))
  val newsForm: Form[PaperNew] = Form(
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "creation_date" -> jodaDate,
      "image" -> text
    )(PaperNew.apply)(PaperNew.unapply)
  )

  def list2(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.news(
      PaperNew.list2(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def addNews(): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request => {
    request.body.file("image")
      .map {
        image => {
          val result = cloudinaryProvider.getClient.uploader().upload(image.ref.file)
          result.onComplete {
            case Success(value) =>
              val data = request.body.dataParts
              val title = data.get("title").map { item => item.head }.head
              val description = data.get("description").map { item => item.head }.head

              val singleNews = new PaperNew(title, description, DateTime.now(), value.url)
              // save to DB
              val savedDevice = PaperNew.save(singleNews)
              val devices = Device.getAllDevices().map(_.token).toList

              val pushBody: Map[String, String] = Map(
                "title" -> savedDevice.title,
                "message" -> savedDevice.description,
                "feed_id" -> String.valueOf(savedDevice.id)
              )
              // send push notifications
              fcmProvider.send(devices, pushBody).map { result => Logger.debug(result.body)}

              val payload: JsObject = Json.obj(
                "id" -> savedDevice.id,
                "title" -> savedDevice.title,
                "description" -> savedDevice.description,
                "creation_date" -> savedDevice.creation_date,
                "image" -> savedDevice.image
              )
              // publish using MQTT
              val mqttPayload: MQTTPayload = MQTTPayload(savedDevice.id, "feed", Json.stringify(payload))
              mqttServiceProvider.publishToTopic(constants.topic, Json.stringify(Json.toJson(mqttPayload)))

            case Failure(exception) =>
              Home.flashing("error" -> "Error during upload".format(exception))
          }
        }
      }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Error occurred"))
    }
  }
    Home.flashing()
  }

  def create: Action[AnyContent] = Action {
    Ok(views.html.createFormNews(newsForm))
  }

  def sendNotification(title: String, message: String, id: Int): Action[AnyContent] = Action.async {
    val devices = Device.getAllDevices().map(_.token).toList

    val body = Map(
      "title" -> title,
      "message" -> message,
      "feed_id" -> String.valueOf(id)
    )

    fcmProvider.send(devices, body).map {
      response => {
        Ok(Json.toJson(Json.obj("code" -> response.status, "status" -> response.statusText, "message" -> response.body)))
      }
    }.recover {
      case t: TimeoutException => InternalServerError(s"Api Timed out $t")
      case t: Throwable => InternalServerError(s"Exception in the api $t")
    }
  }

  def sendNotificationByDeviceId(title: String, message: String, id: Int, deviceId: String): Action[AnyContent] = Action.async {
    Device.findDevice(deviceId).map {
      device => {
        val deviceList = List[String](device.token)
        val body = Map(
          "title" -> title,
          "message" -> message,
          "feed_id" -> String.valueOf(id)
        )
        fcmProvider.send(deviceList, body).map {
          response => {
            Ok(Json.toJson(Json.obj("code" -> response.status, "status" -> response.statusText, "message" -> response.body)))
          }
        }.recover {
          case t: TimeoutException => InternalServerError(s"Api Timed out $t")
          case t: Throwable => InternalServerError(s"Exception in the api $t")
        }
      }
    }.getOrElse {
      Future {
        NotFound(Json.obj("status" -> "fail", "message" -> "Device not found"))
      }
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    newsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormNews(id, formWithErrors)),
      paperNew => {
        PaperNew.update(id, paperNew)
        Home.flashing("success" -> "User %s has been updated".format(paperNew.title))
      }
    )
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    PaperNew.findById(id).map { paperNew =>
      Ok(views.html.editFormNews(id, newsForm.fill(paperNew)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Feed not found"))
    }
  }

  def delete(id: Long): Action[AnyContent] = Action {
    PaperNew.findById(id).map { paperNew =>
      PaperNew.delete(paperNew)
      Home.flashing("success" -> "News has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "News not found")
    }
  }

  def getNews: Action[AnyContent] = Action {
    val news = PaperNew.getFeedList(true)
    Ok(Json.toJson(news))
  }

  def clearNews: Action[AnyContent] = Action {
    PaperNew.clearNews()
    Ok(Json.obj("status" -> "success", "message" -> "News were removed successfully"))
  }

  def getNewsFeed: Action[AnyContent] = Action {
    val newsFeed = PaperNew.getFeedList(true)

    val list = newsFeed.map {
      feed => {
        Json.obj(
          "id" -> feed.id,
          "title" -> feed.title,
          "description" -> feed.description,
          "creation_date" -> feed.creation_date,
          "image" -> feed.image
        )
      }
    }
    Ok(Json.toJson(list))
  }

  def getFeed(offset: Int, limit: Int): Action[AnyContent] = Action {
    val newsFeed = PaperNew.getFeedListWithOffset(reverse = true, offset, limit)

    val list = newsFeed.map {
      feed => {
        Json.obj(
          "id" -> feed.id,
          "title" -> feed.title,
          "description" -> feed.description,
          "creation_date" -> feed.creation_date,
          "image" -> feed.image
        )
      }
    }
    Ok(Json.toJson(list))
  }

  def getSingleNews(id: Long): Action[AnyContent] = Action {
    PaperNew.findById(id).map {
      feed => {
        Ok(Json.toJson(
          Json.obj(
            "id" -> feed.id,
            "title" -> feed.title,
            "description" -> feed.description,
            "creation_date" -> feed.creation_date,
            "image" -> feed.image
          )
        ))
      }
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Feed not found"))
    }
  }

  def publishMessage(message: String): Action[AnyContent] = Action {
    mqttServiceProvider.publishToTopic(constants.topic, message)
    Ok(Json.obj("status" -> "success", "message" -> "success"))
  }
}
