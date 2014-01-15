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


class MongoAndHiveMix extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val default_header = Map(
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/json; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )

  val connect_header = Map(
    "Accept" -> """*/*""",
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/x-www-form-urlencoded; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )

  val hiveHost = System.getProperty("hive.host")
  val mongoHost = System.getProperty("mongo.host")

  val scn = scenario("Query both mongo and hive")
    .exec(http("Register for filter key")
    .post("/neon/services/filterservice/registerforfilterkey")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_7.txt")
  )
    .pause(2)
    .exec(http("Connect to Mongo")
    .post("/neon/services/connectionservice/connect")
    .headers(connect_header)
    .param("datastore", "mongo")
    .param("hostname", mongoHost)
  )
    .pause(5)
    .exec(http("Query for all data")
    .post("/neon/services/queryservice/querydisregardfilters")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_8.txt")
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Add a filter")
    .post("/neon/services/filterservice/addfilter")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_9.txt")
  )
    .pause(1)
    .exec(http("Add a selection")
    .post("/neon/services/selectionservice/addselection")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_10.txt")
  )
    .pause(3)
    .exec(http("Connect to hive")
    .post("/neon/services/connectionservice/connect")
    .headers(connect_header)
    .param( "datastore", "hive")
    .param( "hostname", hiveHost)
  )
    .pause(5)
    .exec(http("Query filtered data from hive")
    .post("/neon/services/queryservice/query")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_12.txt")
    .check(bodyString.is(hive_filtered_data))
  )
    .pause(5)
    .exec(http("Query selection from hive")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_13.txt")
    .check(bodyString.is(hive_selection_data))
  )
    .pause(5)
    .exec(http("Connect to mongo")
    .post("/neon/services/connectionservice/connect")
    .headers(connect_header)
    .param( "datastore", "mongo")
    .param( "hostname", mongoHost)
  )
    .pause(3)
    .exec(http("Query filtered data from mongo")
    .post("/neon/services/queryservice/query")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_15.txt")
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(2)
    .exec(http("Query selection from mongo")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_16.txt")
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(4)
    .exec(http("Remove filter")
    .post("/neon/services/filterservice/removefilter")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_17.txt")
  )
    .pause(1)
    .exec(http("Query selection")
    .post("/neon/services/queryservice/querywithselectiononly")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_18.txt")
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(2)
    .exec(http("Remove selection")
    .post("/neon/services/selectionservice/removeselection")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_19.txt")
  )
    .pause(1)
    .exec(http("Query for filtered data after removing filters and selection")
    .post("/neon/services/queryservice/query")
    .headers(default_header)
    .fileBody("MongoAndHiveMix_request_20.txt")
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
}