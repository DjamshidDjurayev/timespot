package models

import org.joda.time.DateTime
import play.api.libs.json.Json
import sorm.Persisted

/**
 * Created by dzhuraev on 4/20/16.
 */
case class History(income: DateTime, outcome: DateTime, staffer: Staffer)

object History {
  implicit val historyFormat = Json.format[History]
}
