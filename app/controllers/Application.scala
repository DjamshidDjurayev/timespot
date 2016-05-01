package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Flash

class Application extends Controller {

  def index = Action {
    Ok(views.html.main2("as"))
  }

}
