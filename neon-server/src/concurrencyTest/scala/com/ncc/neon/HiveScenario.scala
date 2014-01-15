package com.ncc.neon

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._
import assertions._
import Headers._
import Responses._

class HiveScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val hiveHost = System.getProperty("hive.host")

  val scn = scenario("Hive under 10 concurrent users")
    .exec(http("Register for a filter key")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(json_header)
    .fileBody("HiveScenario_request_4.txt")
  )
    .pause(2)
    .exec(http("Connect to Hive")
    .post("/neon/services/connectionservice/connect")
    .headers(connect_header)
    .param("datastore", "hive")
    .param("hostname", hiveHost)
  )
    .pause(5)
    .exec(http("Query for all data")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(json_header)
    .fileBody("HiveScenario_request_6.txt")
    .check(bodyString.is(all_data))
  )
    .pause(2)
    .exec(http("Add a filter")
    .post("/neon/services/filterservice/addfilter")
    .headers(json_header)
    .fileBody("HiveScenario_request_7.txt")
  )
    .pause(1)
    .exec(http("Add a selection")
    .post("/neon/services/selectionservice/addselection")
    .headers(json_header)
    .fileBody("HiveScenario_request_8.txt")
  )
    .pause(25)
    .exec(http("Query for the selection")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(json_header)
    .fileBody("HiveScenario_request_9.txt")
    .check(bodyString.is(selection_data))
  )
    .pause(2)
    .exec(http("Remove the filter")
    .post("/neon/services/filterservice/removefilter")
    .headers(json_header)
    .fileBody("HiveScenario_request_10.txt")
  )
    .pause(20)
    .exec(http("Query for filtered data")
    .post("/neon/services/queryservice/query")
    .headers(json_header)
    .fileBody("HiveScenario_request_11.txt")
    .check(bodyString.is(all_data))
  )
    .pause(2)
    .exec(http("Remove the selection")
    .post("/neon/services/selectionservice/removeselection")
    .headers(json_header)
    .fileBody("HiveScenario_request_12.txt")
  )
    .pause(20)
    .exec(http("Query for selection after it was removed")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(json_header)
    .fileBody("HiveScenario_request_13.txt")
    .check(bodyString.is(all_data))
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
}