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
    Entity[Admin]()),
    url = "jdbc:postgresql://ec2-174-129-18-170.compute-1.amazonaws.com:5432/dapr1bg4t9b4l9",
//    url = "jdbc:postgresql://localhost/timespot_db",
    user = "slfsepqvvvepcc",
    password = "bIiQwGhngRHc4dTXpPKUaKySAK",
//    user = "postgres",
//    password = "postgres",
    initMode = InitMode.Create
)

