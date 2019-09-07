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
    Entity[History](),
    Entity[Admin](),
    Entity[Device]()),
  url = "jdbc:postgresql://ec2-174-129-18-170.compute-1.amazonaws.com:5432/dd83d204polrve",
  //  url = "jdbc:h2:mem:play",
  //  url = "jdbc:postgresql://localhost/timespot_db",
  user = "ntlmnyvtuvpwpv",
  password = "_AwM27MHmyBzkIXPPDOVu1I32L",
  initMode = InitMode.Create
)

