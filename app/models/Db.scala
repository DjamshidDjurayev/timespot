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
    Entity[Device]()
  ),
  url = constants.DB_URL,
  user = constants.DB_USER,
  password = constants.DB_PASSWORD,
  initMode = InitMode.DropAllCreate
)

