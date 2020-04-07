package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Question(question: String, answer: String, date: Long)

case class QuestionPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Question {
  implicit val format: Format[Question] = Json.format[Question]

  def questionList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): QuestionPage[Question] = {
    val offset = pageSize * page
    val contacts = Db.query[Question].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Question].count()
    QuestionPage(contacts, page, offset, totalRows)
  }

  def save(question: Question): Question with Persisted = {
    Db.save[Question](question)
  }

  def saveContact(question: String, answer: String, date: Long): Question with Persisted = {
    val item = new Question(question, answer, date)
    Db.save[Question](item)
  }

  def getQuestions(): Stream[Question with Persisted] = {
    Db.query[Question].order("id", reverse = true).fetch()
  }

  def findById(id: Long): Option[Question with Persisted] = {
    Db.query[Question].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, question: Question): List[Question with Persisted] = {
    Db.query[Question].whereEqual("id", id).replace(question)
  }

  def delete(question: Question): Unit = {
    Db.delete[Question](question)
  }
}
