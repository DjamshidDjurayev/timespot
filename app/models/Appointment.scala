package models

import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date

import play.api.libs.json.{Format, Json}
import sorm.Persisted

case class Appointment(startTime: String, endTime: String, createdAt: Long)

case class AppointmentPage[+A](items: Seq[A with Persisted], page: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ >= 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Appointment {
  implicit val format: Format[Appointment] = Json.format[Appointment]

  def appointmentsList(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): AppointmentPage[Appointment] = {
    val offset = pageSize * page
    val contacts = Db.query[Appointment].limit(pageSize).order("id", reverse = true).offset(offset).fetch()
    val totalRows = Db.query[Appointment].count()
    AppointmentPage(contacts, page, offset, totalRows)
  }

  def save(appointment: Appointment): Appointment with Persisted = {
    Db.save[Appointment](appointment)
  }

  def getAppointments(): Stream[Appointment with Persisted] = {
    Db.query[Appointment].order("id", reverse = true).fetch()
  }

  def getAppointmentsByDate(date: Long): Stream[Appointment with Persisted] = {
    val appointments = Db.query[Appointment].order("id", reverse = false).fetch().filter(appointment => {

      val dateFormatter = new SimpleDateFormat("YYYY-mm-dd")

      val appointmentDate = dateFormatter.format(new Date(appointment.createdAt))
      val searchingDate = dateFormatter.format(new Date(date))

      appointmentDate == searchingDate
    })

    appointments
  }

  def findById(id: Long): Option[Appointment with Persisted] = {
    Db.query[Appointment].whereEqual("id", id).fetchOne()
  }

  def update(id: Long, appointment: Appointment): List[Appointment with Persisted] = {
    Db.query[Appointment].whereEqual("id", id).replace(appointment)
  }

  def delete(appointment: Appointment): Unit = {
    Db.delete[Appointment](appointment)
  }
}
