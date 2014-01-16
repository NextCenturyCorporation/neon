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
  val serviceRoot = "/neon/services/"

  val scn = scenario("Query both mongo and hive")
    .exec(http("Register for filter key")
    .post(serviceRoot + "filterservice/registerforfilterkey")
    .headers(default_header)
    .body(connect_request)
  )
    .pause(2)
    .exec(http("Connect to Mongo")
    .post(serviceRoot + "connectionservice/connect")
    .headers(connect_header)
    .param("datastore", "mongo")
    .param("hostname", mongoHost)
  )
    .pause(5)
    .exec(http("Query for all data")
    .post(serviceRoot + "queryservice/querydisregardfilters")
    .headers(default_header)
    .body(all_data_query)
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Add a filter")
    .post(serviceRoot + "filterservice/addfilter")
    .headers(default_header)
    .body(add_filter)
  )
    .pause(1)
    .exec(http("Add a selection")
    .post(serviceRoot + "selectionservice/addselection")
    .headers(default_header)
    .body(add_selection)
  )
    .pause(3)
    .exec(http("Connect to hive")
    .post(serviceRoot + "connectionservice/connect")
    .headers(connect_header)
    .param( "datastore", "hive")
    .param( "hostname", hiveHost)
  )
    .pause(5)
    .exec(http("Query filtered data from hive")
    .post(serviceRoot + "queryservice/query")
    .headers(default_header)
    .body(filtered_query)
    .check(bodyString.is(hive_filtered_data))
  )
    .pause(5)
    .exec(http("Query selection from hive")
    .post(serviceRoot + "queryservice/querywithselectiononly")
    .headers(default_header)
    .body(selection_only_query)
    .check(bodyString.is(hive_selection_data))
  )
    .pause(5)
    .exec(http("Connect to mongo")
    .post(serviceRoot + "connectionservice/connect")
    .headers(connect_header)
    .param( "datastore", "mongo")
    .param( "hostname", mongoHost)
  )
    .pause(3)
    .exec(http("Query filtered data from mongo")
    .post(serviceRoot + "queryservice/query")
    .headers(default_header)
    .body(filtered_query)
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(2)
    .exec(http("Query selection from mongo")
    .post(serviceRoot + "queryservice/querywithselectiononly")
    .headers(default_header)
    .body(selection_only_query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(4)
    .exec(http("Remove filter")
    .post(serviceRoot + "filterservice/removefilter")
    .headers(default_header)
    .body(filter_key)
  )
    .pause(1)
    .exec(http("Query selection")
    .post(serviceRoot + "queryservice/querywithselectiononly")
    .headers(default_header)
    .body(selection_only_query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(2)
    .exec(http("Remove selection")
    .post(serviceRoot + "selectionservice/removeselection")
    .headers(default_header)
    .body(filter_key)
  )
    .pause(1)
    .exec(http("Query for filtered data after removing filters and selection")
    .post(serviceRoot + "queryservice/query")
    .headers(default_header)
    .body(filtered_query)
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
}