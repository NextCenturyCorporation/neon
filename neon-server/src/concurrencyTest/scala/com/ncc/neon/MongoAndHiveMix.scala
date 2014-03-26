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

  val serviceRoot = "/neon/services/"
  val mongoQueryServicePath = mongoHost + "/mongo"
  val hiveQueryServicePath = hiveHost + "/hive"

  val scn = scenario("Query both mongo and hive")
    .exec(http("Query for all data")
    .post(serviceRoot + "queryservice/querydisregardfilters/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_all_data))
  )
    .pause(1)
    .exec(http("Add a filter")
    .post(serviceRoot + "filterservice/addfilter")
    .headers(json_header)
    .body(add_filter)
  )
    .pause(1)
    .exec(http("Add a selection")
    .post(serviceRoot + "selectionservice/addselection")
    .headers(json_header)
    .body(add_selection)
  )
    .pause(3)
    .exec(http("Query filtered data from hive")
    .post(serviceRoot + "queryservice/query/" + hiveQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(hive_filtered_data))
  )
    .pause(5)
    .exec(http("Query selection from hive")
    .post(serviceRoot + "queryservice/querywithselectiononly/" + hiveQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(hive_selection_data))
  )
    .pause(5)
    .exec(http("Query filtered data from mongo")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(2)
    .exec(http("Query selection from mongo")
    .post(serviceRoot + "queryservice/querywithselectiononly/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(4)
    .exec(http("Remove filter")
    .post(serviceRoot + "filterservice/removefilter")
    .headers(text_header)
    .body(filterId)
  )
    .pause(1)
    .exec(http("Query selection")
    .post(serviceRoot + "queryservice/querywithselectiononly/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(2)
    .exec(http("Remove selection")
    .post(serviceRoot + "selectionservice/removeselection")
    .headers(text_header)
    .body(filterId)
  )
    .pause(1)
    .exec(http("Query for filtered data after removing filters and selection")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}