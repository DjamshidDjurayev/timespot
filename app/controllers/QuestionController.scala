package controllers

import com.google.inject.Inject
import models.{Contacts, Question}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.ExecutionContext

class QuestionController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.QuestionController.questionList(0, 2, ""))

  val questionForm: Form[Question] = Form(
    mapping(
      "question" -> text,
      "answer" -> text
    )( (question, answer) => Question(question, answer, System.currentTimeMillis()))( (question: Question) => Some(question.question, question.answer))
  )

  def questionList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.questions(
      Question.questionList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormQuestions(questionForm))
  }

  def questionsForm(): Action[AnyContent] = Action { implicit request =>
    questionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormQuestions(formWithErrors)),
      question => {
        Question.save(question)
        Home.flashing("success" -> "Question %s has been created".format(question.question))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Question.findById(id).map { question =>
      Question.delete(question)
      Home.flashing("success" -> "Question has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Question not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Question.findById(id).map { question =>
      Ok(views.html.editFormQuestions(id, questionForm.fill(question)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Question not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    questionForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormQuestions(id, formWithErrors)),
      question => {
        Question.update(id, question)
        Home.flashing("success" -> "Question %s has been updated".format(question.question))
      }
    )
  }

  def getQuestions: Action[AnyContent] = Action {
    val questions = Question.getQuestions()

    val questionList = questions.map {
      question => {
        Json.obj(
          "id" -> question.id,
          "question" -> question.question,
          "answer" -> question.answer,
          "date" -> question.date
        )
      }
    }
    Ok(Json.toJson(questionList))
  }
}
