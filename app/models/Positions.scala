package models

import play.api.libs.json.Json

/**
 * Created by dzhuraev on 3/30/16.
 */
case class Positions(title: String)

object Positions {
  implicit val positionsFormat = Json.format[Positions]



  def getAllPositions: Seq[(String, String)] = {
    DBase.query[Positions].fetch().map(c => (c.title, c.title ))
    }
  }
