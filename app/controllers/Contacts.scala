package controllers

import play.api.mvc._

class Contacts extends Controller {

  def index = Action {
    Ok(views.html.contacts("About"))

  }




}
