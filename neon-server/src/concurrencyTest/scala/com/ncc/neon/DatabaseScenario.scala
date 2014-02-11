package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._
import Headers._
import Requests._

class DatabaseScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val serviceRoot = "/neon/services/"
  val connectionId = "mongo@" + mongoHost

  val scn = scenario("Query for database and table names")
    .exec(http("Create a connection")
    .post(serviceRoot + "connections")
    .headers(json_header)
    .body(mongo_connection_request))
    .pause(2)

    .exec(http("Get Database Names")
    .get("http://localhost:11402/neon/services/queryservice/" + connectionId + "/databasenames")
    .check(jsonPath("$[?(@=='concurrencytest')]").exists))

    .exec(http("Get Table Names")
    .post("http://localhost:11402/neon/services/queryservice/" + connectionId + "/tablenames")
    .headers(form_header)
    .param("database", "concurrencytest")
    .check(jsonPath("$[?(@=='records')]").exists))


  setUp(scn.users(2).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}