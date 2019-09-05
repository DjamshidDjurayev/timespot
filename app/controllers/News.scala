package controllers

import com.cloudinary.Cloudinary
import models.{Db, Device, GcmRestServer, PaperNew}
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.Files
import play.api.libs.json.Json

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class News extends Controller {

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

  val cloudinary = new Cloudinary(Map(
    "cloud_name" -> "ds5cpnnkl",
    "api_key" -> "566579673111817",
    "api_secret" -> "gx5pxezG25hPdqvNEEk8GWdeTcQ"
  ))

  def addNews(): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { implicit request => {
    request.body.file("image")
      .map {
        image => {
          val result = cloudinary.uploader().upload(image.ref.file)
          result.onComplete {
            case Success(value) =>
              val data = request.body.dataParts
              val title = data.get("title").map { item => item.head }.head
              val description = data.get("description").map { item => item.head }.head
              val creationDate = data.get("creation_date").map { item => item.head }.head

              val singleNews = new PaperNew(title, description, new DateTime(creationDate), value.url)
              val savedDevice = Db.save[PaperNew](singleNews)

              val server = new GcmRestServer("AAAAIr9zZnk:APA91bFuiPycVWFSclIlcxZOmkgTD_QPk1nxtTAnJjj1NbvzMvmSKZXjBT2Tr18NYOncwgyjI1PkeGauivrvTZINqTSPcCaOonx83bplyETRRpophuYNvSyPNkkFM0AtlKFeLh6S3sEn")
              val devices = Device.getAllDevices().map(_.token).toList

              server.send(devices, Map(
                "title" -> savedDevice.title,
                "message" -> savedDevice.description,
                "feed_id" -> String.valueOf(savedDevice.id)
              ))

            case Failure(exception) =>
              Home.flashing("error" -> "Error during upload".format(exception))
          }
        }
      }.getOrElse(NotFound)
  }
    Home.flashing()
  }

  def create: Action[AnyContent] = Action {
    Ok(views.html.createFormNews(newsForm))
  }

  def sendNotification(title: String, message: String, id: Int): Action[AnyContent] = Action {
    val server = new GcmRestServer("AAAAIr9zZnk:APA91bFuiPycVWFSclIlcxZOmkgTD_QPk1nxtTAnJjj1NbvzMvmSKZXjBT2Tr18NYOncwgyjI1PkeGauivrvTZINqTSPcCaOonx83bplyETRRpophuYNvSyPNkkFM0AtlKFeLh6S3sEn")
    val devices = Device.getAllDevices().map(_.token).toList

    server.send(devices, Map(
      "title" -> title,
      "message" -> message,
      "feed_id" -> String.valueOf(id)
    ))
    Ok("success")
  }

  def sendNotificationByDeviceId(title: String, message: String, id: Int, deviceId: String): Action[AnyContent] = Action {
    val server = new GcmRestServer("AAAAIr9zZnk:APA91bFuiPycVWFSclIlcxZOmkgTD_QPk1nxtTAnJjj1NbvzMvmSKZXjBT2Tr18NYOncwgyjI1PkeGauivrvTZINqTSPcCaOonx83bplyETRRpophuYNvSyPNkkFM0AtlKFeLh6S3sEn")
    Device.findDevice(deviceId).map {
      device => {
        server.send(List[String](device.token), Map(
          "title" -> title,
          "message" -> message,
          "feed_id" -> String.valueOf(id)
        ))
      }
    }.getOrElse(NotFound)
    Ok(Json.obj("status" -> "success", "message" -> "Notifications sent successfully"))
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
    }.getOrElse(NotFound)
  }

  def delete(id: Long): Action[AnyContent] = Action {
    PaperNew.delete(id)
    Home.flashing("success" -> "News has been deleted")
  }

  def getNews: Action[AnyContent] = Action {
    val news = Db.query[PaperNew].order("id", reverse = true).fetch()
    Ok(Json.toJson(news))
  }

  def clearNews: Action[AnyContent] = Action {
    PaperNew.clearNews()
    Ok(Json.obj("status" -> "success", "message" -> "News were removed successfully"))
  }

  def getNewsFeed: Action[AnyContent] = Action {
    val newsFeed = Db.query[PaperNew].order("id", reverse = true).fetch()

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
    }.getOrElse(NotFound)
  }

}
