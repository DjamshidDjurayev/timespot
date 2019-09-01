package controllers

import java.nio.file.Paths

import akka.event.Logging
import com.cloudinary.Cloudinary
import models.{Db, PaperNew, Staffer}
import org.joda.time.DateTime
import play.api.{Logger, Play}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.libs.Files
import play.api.libs.json.Json


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
      PaperNew.list2(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }

  val cloudinary = new Cloudinary(Map(
    "cloud_name" -> "ds5cpnnkl",
    "api_key" -> "566579673111817",
    "api_secret" -> "gx5pxezG25hPdqvNEEk8GWdeTcQ"
  ))

  def addNews(): Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { implicit request => {
    val imageName = ""
    request.body.file("image")
        .map {
          image => {
            val filename = image.filename
            cloudinary.uploader().upload(filename)
          }
        }

    val data = request.body.dataParts

    val title = data.get("title").map { item => item.head }.head
    val description = data.get("description").map { item => item.head }.head
    val creationDate = data.get("creation_date").map { item => item.head }.head

    val singleNews = new PaperNew(title, description, new DateTime(creationDate), imageName)

    Db.save[PaperNew](singleNews)
    Home.flashing("success" -> "News %s has been created".format(singleNews.title))

//    newsForm.bindFromRequest(request.body.asFormUrlEncoded).fold(
//      formWithErrors => BadRequest(views.html.createFormNews(formWithErrors)),
//      newsPaper => {
//        Db.save[PaperNew](newsPaper)
//        Home.flashing("success" -> "News %s has been created".format(newsPaper.title))
//      }
//    )
  }
  }

  def create: Action[AnyContent] = Action {
    Ok(views.html.createFormNews(newsForm))
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

  def getOneNews(id: Long): Action[AnyContent] = Action {
    Ok(Json.toJson(PaperNew.findById(id)))
  }
}
