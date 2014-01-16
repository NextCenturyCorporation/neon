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
  val serviceRoot = "/neon/services/"

  val scn = scenario("Mongo under 10 concurrent users")
    .exec(http("Register for filter key")
    .post(serviceRoot + "filterservice/registerforfilterkey")
    .headers(json_header)
    .body(connect_request)
  )
    .pause(2)
    .exec(http("Connect to Mongo")
    .post(serviceRoot + "connectionservice/connect")
    .headers(connect_header)
    .param("datastore", "mongo")
    .param("hostname", mongoHost)
  )
    .pause(2)
    .exec(http("Add Filter")
    .post(serviceRoot + "filterservice/addfilter")
    .headers(json_header)
    .body(add_filter)
  )
    .pause(1)
    .exec(http("Add selection")
    .post(serviceRoot + "selectionservice/addselection")
    .headers(json_header)
    .body(add_selection)
  )
    .pause(1)
    .exec(http("Query for all data")
    .post(serviceRoot + "queryservice/querydisregardfilters")
    .headers(json_header)
    .body(all_data_query)
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Query for filtered data")
    .post(serviceRoot + "queryservice/query")
    .headers(json_header)
    .body(filtered_query)
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(1)
    .exec(http("Query for selection")
    .post(serviceRoot + "queryservice/querywithselectiononly")
    .headers(json_header)
    .body(selection_only_query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(1)
    .exec(http("Remove selection")
    .post(serviceRoot + "selectionservice/removeselection")
    .headers(json_header)
    .body(filter_key)
  )
    .pause(2)
    .exec(http("Remove filter")
    .post(serviceRoot + "filterservice/removefilter")
    .headers(json_header)
    .body(filter_key)
  )
    .pause(2)
    .exec(http("Query for selection after it was removed")
    .post(serviceRoot + "queryservice/querywithselectiononly")
    .headers(json_header)
    .body(selection_only_query)
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Query for filtered data after the filter was removed")
    .post(serviceRoot + "queryservice/query")
    .headers(json_header)
    .body(filtered_query)
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(12).ramp(3).protocolConfig(httpConf))
}