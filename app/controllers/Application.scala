package controllers

import play.api.mvc._

class Application extends Controller {
  def index: Action[AnyContent] = Action {
    Ok(views.html.main2("as"))
  }
}
