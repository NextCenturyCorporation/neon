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
import Requests._

class MongoScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .disableFollowRedirect
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val mongoHost = System.getProperty("mongo.host")

  val scn = scenario("Mongo under 10 concurrent users")
    .exec(http("Register for filter key")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(json_header)
    .body(connect_request)
  )
    .pause(2)
    .exec(http("Connect to Mongo")
    .post("/neon/services/connectionservice/connect")
    .headers(connect_header)
    .param("datastore", "mongo")
    .param("hostname", mongoHost)
  )
    .pause(2)
    .exec(http("Add Filter")
    .post("/neon/services/filterservice/addfilter")
    .headers(json_header)
    .fileBody("MongoScenario_request_8.txt")
  )
    .pause(1)
    .exec(http("Add selection")
    .post("/neon/services/selectionservice/addselection")
    .headers(json_header)
    .fileBody("MongoScenario_request_9.txt")
  )
    .pause(1)
    .exec(http("Query for all data")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(json_header)
    .fileBody("MongoScenario_request_10.txt")
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Query for filtered data")
    .post("/neon/services/queryservice/query")
    .headers(json_header)
    .fileBody("MongoScenario_request_11.txt")
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(1)
    .exec(http("Query for selection")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(json_header)
    .fileBody("MongoScenario_request_12.txt")
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(1)
    .exec(http("Remove selection")
    .post("/neon/services/selectionservice/removeselection")
    .headers(json_header)
    .fileBody("MongoScenario_request_13.txt")
  )
    .pause(2)
    .exec(http("Remove filter")
    .post("/neon/services/filterservice/removefilter")
    .headers(json_header)
    .fileBody("MongoScenario_request_14.txt")
  )
    .pause(2)
    .exec(http("Query for selection after it was removed")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(json_header)
    .fileBody("MongoScenario_request_15.txt")
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Query for filtered data after the filter was removed")
    .post("/neon/services/queryservice/query")
    .headers(json_header)
    .fileBody("MongoScenario_request_16.txt")
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(12).ramp(3).protocolConfig(httpConf))
}