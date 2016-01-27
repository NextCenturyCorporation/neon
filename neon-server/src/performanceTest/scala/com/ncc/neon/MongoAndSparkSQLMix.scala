/*
 * Copyright 2016 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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


class MongoAndSparkSQLMix extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val serviceRoot = "/neon/services/"
  val mongoQueryServicePath = mongoHost + "/mongo"
  val sparkSQLQueryServicePath = sparkSQLHost + "/sparksql"

  val scn = scenario("Query both mongo and spark sql")
    .exec(http("Query ignore filters")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .queryParam("ignoreFilters","true")
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
    .exec(http("Query filtered data from spark sql")
    .post(serviceRoot + "queryservice/query/" + sparkSQLQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(spark_sql_filtered_data))
  )
    .pause(5)
    .exec(http("Query selection from spark sql")
    .post(serviceRoot + "queryservice/query/" + sparkSQLQueryServicePath)
    .queryParam("selectionOnly","true")
    .headers(json_header)
    .body(query)
    .check(bodyString.is(spark_sql_selection_data))
  )
    .pause(5)
    .exec(http("Query mongo")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(2)
    .exec(http("Query selection from mongo")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .queryParam("selectionOnly","true")
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
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .queryParam("selectionOnly","true")
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
    .exec(http("Query after removing filters and selection")
    .post(serviceRoot + "queryservice/query/" + mongoQueryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(10).ramp(5).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}