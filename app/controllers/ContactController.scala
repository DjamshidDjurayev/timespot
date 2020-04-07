package controllers

import com.google.inject.Inject
import models.{Admin, Contacts}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.ExecutionContext

class ContactController @Inject()(implicit context: ExecutionContext, components: ControllerComponents) extends AbstractController(components) with I18nSupport {
  val Home: Result = Redirect(routes.ContactController.contactsList(0, 2, ""))

  val contactsForm: Form[Contacts] = Form(
    mapping(
      "city" -> text,
      "phone" -> text,
      "address" -> text
    )(Contacts.apply)(Contacts.unapply)
  )

  def contactsList(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.contacts(
      Contacts.contactList(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
      orderBy, filter
    ))
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.createFormContacts(contactsForm))
  }

  def contactForm(): Action[AnyContent] = Action { implicit request =>
    contactsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.createFormContacts(formWithErrors)),
      contact => {
        Contacts.save(contact)
        Home.flashing("success" -> "Contact %s has been created".format(contact.city))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    Contacts.findById(id).map { contact =>
      Contacts.delete(contact)
      Home.flashing("success" -> "Contact has been deleted")
    }.getOrElse {
      Home.flashing("fail" -> "Contact not found")
    }
  }

  def edit(id: Long): Action[AnyContent] = Action { implicit request =>
    Contacts.findById(id).map { contact =>
      Ok(views.html.editFormContacts(id, contactsForm.fill(contact)))
    }.getOrElse {
      NotFound(Json.obj("status" -> "fail", "message" -> "Contact not found"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    contactsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.editFormContacts(id, formWithErrors)),
      contact => {
        Contacts.update(id, contact)
        Home.flashing("success" -> "Contact %s has been updated".format(contact.city))
      }
    )
  }

  def getContacts: Action[AnyContent] = Action {
    val contacts = Contacts.getContacts()

    val contactsList = contacts.map {
      contact => {
        Json.obj(
          "id" -> contact.id,
          "city" -> contact.city,
          "phone" -> contact.phone,
          "address" -> contact.address
        )
      }
    }
    Ok(Json.toJson(contactsList))
  }
}
