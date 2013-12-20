package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._

class SimpleScenario extends Simulation {

  val scn = scenario("My Simple Scenario")
    .exec(http("Neon Field Names")
    .get("http://localhost:11402/neon/services/queryservice/fields?databaseName=mydb&tableName=sample&widgetName="))

  setUp(scn.users(2))
}