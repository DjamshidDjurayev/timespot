package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Flash

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("home"))
  }

  def about = Action {
    Ok(views.html.about("about"))
  }

  def contacts = Action {
    Ok(views.html.contacts("contacts"))
  }


}
