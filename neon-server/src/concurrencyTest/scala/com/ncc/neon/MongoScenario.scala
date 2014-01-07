package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._
import assertions._

class MongoScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .disableFollowRedirect
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val default_headers = Map(
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/json; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )


  val scn = scenario("Mongo under 10 concurrent users")
    .exec(http("Register for filter key")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(default_headers)
    .fileBody("MongoScenario_request_5.txt")
  )
    .pause(5)
    .exec(http("Add Filter")
    .post("/neon/services/filterservice/addfilter")
    .headers(default_headers)
    .fileBody("MongoScenario_request_8.txt")
  )
    .pause(865 milliseconds)
    .exec(http("Add selection")
    .post("/neon/services/selectionservice/addselection")
    .headers(default_headers)
    .fileBody("MongoScenario_request_9.txt")
  )
    .pause(1)
    .exec(http("Query for all data")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(default_headers)
    .fileBody("MongoScenario_request_10.txt")
  )
    .pause(1)
    .exec(http("Query for filtered data")
    .post("/neon/services/queryservice/query")
    .headers(default_headers)
    .fileBody("MongoScenario_request_11.txt")
  )
    .pause(1)
    .exec(http("Query for selection")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(default_headers)
    .fileBody("MongoScenario_request_12.txt")
  )
    .pause(1)
    .exec(http("Remove selection")
    .post("/neon/services/selectionservice/removeselection")
    .headers(default_headers)
    .fileBody("MongoScenario_request_13.txt")
  )
    .pause(2)
    .exec(http("Remove filter")
    .post("/neon/services/filterservice/removefilter")
    .headers(default_headers)
    .fileBody("MongoScenario_request_14.txt")
  )
    .pause(2)
    .exec(http("Query for selection after it was removed")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(default_headers)
    .fileBody("MongoScenario_request_15.txt")
  )
    .pause(1)
    .exec(http("Query for filtered data after the filter was removed")
    .post("/neon/services/queryservice/query")
    .headers(default_headers)
    .fileBody("MongoScenario_request_16.txt")
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
}