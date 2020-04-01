package controllers

import com.google.inject.Inject
import models.{Admin, Call, Review}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class Reviews @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {

  def addReview(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val userId = (json \ "id").as[Long]
      val name = (json \ "name").as[String]
      val comment = (json \ "comment").as[String]
      val rating = (json \ "rating").as[Double]
      val siteIncluded = (json \ "siteIncluded").as[Boolean]
      val socialIncluded = (json \ "socialIncluded").as[Boolean]
      val toDirector = (json \ "toDirector").as[Boolean]

      val review = new Review(userId, name, comment, rating, siteIncluded, socialIncluded, toDirector)
      val savedReview = Review.save(review)

      if (savedReview != null) {
        Ok(Json.toJson(
          Json.obj(
            "status" -> "success",
            "code" -> 200
          )
        ))
      } else {
        BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def callOrder(): Action[AnyContent] = Action { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json => {
      val userId = (json \ "id").as[Long]
      val name = (json \ "name").as[String]
      val phone = (json \ "phone").as[String]

      val call = new Call(userId, name, phone)
      val savedCall = Call.save(call)

      if (savedCall != null) {
        Ok(Json.toJson(
          Json.obj(
            "status" -> "success",
            "code" -> 200
          )
        ))
      } else {
        BadRequest(Json.obj("status" -> "fail", "message" -> "Internal error"))
      }
    }
    }.getOrElse {
      BadRequest(Json.obj("status" -> "fail", "message" -> "Expecting application/json request body"))
    }
  }

  def getReviews: Action[AnyContent] = Action {
    val reviews = Review.getReviews()

    val list = reviews.map {
      review => {
        Json.obj(
          "id" -> review.id,
          "userId" -> review.userId,
          "name" -> review.name,
          "comment" -> review.comment,
          "rating" -> review.rating,
          "siteIncluded" -> review.siteIncluded,
          "socialIncluded" -> review.socialIncluded,
          "toDirector" -> review.toDirector
        )
      }
    }
    Ok(Json.toJson(list))
  }

  def getCalls: Action[AnyContent] = Action {
    val calls = Call.getCalls()

    val list = calls.map {
      call => {
        Json.obj(
          "id" -> call.id,
          "userId" -> call.userId,
          "name" -> call.name,
          "phone" -> call.phone
        )
      }
    }
    Ok(Json.toJson(list))
  }
}
