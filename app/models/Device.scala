package models

import play.api.libs.json.{Format, Json}
import sorm.Persisted

/**
 * Created by dzhuraev on 5/12/16.
 */
case class Device(deviceId: String, token: String)

object Device {

  implicit val format: Format[Device] = Json.format[Device]

  def getAllDevices(): Stream[Device with Persisted] = {
    Db.query[Device].fetch()
  }

  def saveDevice(deviceId: String, tokenId: String): Device with Persisted = {
    val device = new Device(deviceId, tokenId)
    Db.save[Device](device)
  }

  def removeDevice(device: Device): Unit = {
    Db.delete[Device](device)
  }

  def updateDevice(device: Device): Device with Persisted = {
    Db.save[Device](device)
  }

  def updateDeviceByDeviceId(device: Device, token: String): Device with Persisted = {
    Db.save[Device](device.copy(token = token))
  }

  def findDevice(deviceId: String): Option[Device with Persisted] = {
    Db.query[Device].whereEqual("deviceId", deviceId).fetchOne()
  }

  def removeAllDevices(): Unit = {
    val devices = getAllDevices()
    for (device <- devices) {
      Db.delete[Device](device)
    }
  }

}
