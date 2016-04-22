package controllers

import play.api.mvc._

class About extends Controller {

  def index = Action {
    Ok(views.html.about("About"))

  }

}
