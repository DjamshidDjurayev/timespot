package models

/**
 * Created by dzhuraev on 3/15/16.
 */

import sorm._

object Db extends Instance(
  entities = Set(
    Entity[Staffer](),
    Entity[PaperNew](),
    Entity[Positions](),
    Entity[StaffHistory](),
    Entity[Admin](),
    Entity[Device](),
    Entity[Alert](),
    Entity[Review](),
    Entity[Call](),
    Entity[Contacts](),
    Entity[Question](),
    Entity[Tariff](),
    Entity[Assigner](),
    Entity[TariffOption](),
    Entity[Recommendation](),
    Entity[MedHistory](),
    Entity[Payment](),
    Entity[Room](),
    Entity[Message](),
    Entity[Appointment](),
    Entity[City](),
    Entity[PaymentSchedule]()
  ),
  url = constants.DB_URL,
  user = constants.DB_USER,
  password = constants.DB_PASSWORD,
  initMode = InitMode.Create
)

