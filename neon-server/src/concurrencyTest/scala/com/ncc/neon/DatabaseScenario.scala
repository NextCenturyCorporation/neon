package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._

class DatabaseScenario extends Simulation {

  val scn = scenario("Query for databases")
    .exec(http("Get Database Names").get("http://localhost:11402/neon/services/queryservice/databasenames"))

  setUp(scn.users(2))
}