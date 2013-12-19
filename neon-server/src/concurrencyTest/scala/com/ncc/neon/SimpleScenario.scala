package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._

class SimpleScenario extends Simulation {

  val scn = scenario("My Simple Scenario")
    .exec(http("My favorite search engine")
    .get("http://www.google.com"))

  setUp(scn.users(2))
}