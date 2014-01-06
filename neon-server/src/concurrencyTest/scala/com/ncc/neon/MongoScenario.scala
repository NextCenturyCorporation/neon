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

  val headers_5 = Map(
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/json; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )


  val scn = scenario("Scenario Name")
    .exec(http("request_5")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(headers_5)
    .fileBody("MongoScenario_request_5.txt")
  )
    .pause(83 milliseconds)
    .exec(http("request_7")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(headers_5)
    .fileBody("MongoScenario_request_7.txt")
  )
    .pause(5)
    .exec(http("request_8")
    .post("/neon/services/filterservice/addfilter")
    .headers(headers_5)
    .fileBody("MongoScenario_request_8.txt")
  )
    .pause(865 milliseconds)
    .exec(http("request_9")
    .post("/neon/services/selectionservice/addselection")
    .headers(headers_5)
    .fileBody("MongoScenario_request_9.txt")
  )
    .pause(1)
    .exec(http("request_10")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(headers_5)
    .fileBody("MongoScenario_request_10.txt")
  )
    .pause(1)
    .exec(http("request_11")
    .post("/neon/services/queryservice/query")
    .headers(headers_5)
    .fileBody("MongoScenario_request_11.txt")
  )
    .pause(1)
    .exec(http("request_12")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(headers_5)
    .fileBody("MongoScenario_request_12.txt")
  )
    .pause(1)
    .exec(http("request_13")
    .post("/neon/services/selectionservice/removeselection")
    .headers(headers_5)
    .fileBody("MongoScenario_request_13.txt")
  )
    .pause(2)
    .exec(http("request_14")
    .post("/neon/services/filterservice/removefilter")
    .headers(headers_5)
    .fileBody("MongoScenario_request_14.txt")
  )
    .pause(2)
    .exec(http("request_15")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(headers_5)
    .fileBody("MongoScenario_request_15.txt")
  )
    .pause(1)
    .exec(http("request_16")
    .post("/neon/services/queryservice/query")
    .headers(headers_5)
    .fileBody("MongoScenario_request_16.txt")
  )
    .pause(1)
    .exec(http("request_17")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(headers_5)
    .fileBody("MongoScenario_request_17.txt")
  )

  setUp(scn.users(10).ramp(10).protocolConfig(httpConf))
}