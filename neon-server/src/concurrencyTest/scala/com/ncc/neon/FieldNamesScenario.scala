package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._

class FieldNamesScenario extends Simulation {

  val scn = scenario("Query for field names")
    .exec(http("Neon Field Names")
    .get("http://localhost:11402/neon/services/queryservice/fields?databaseName=db&tableName=table&widgetName=widget"))

  setUp(scn.users(2))
}