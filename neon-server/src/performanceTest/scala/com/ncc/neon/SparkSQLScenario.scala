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

class SparkSQLScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val serviceRoot = "/neon/services/"
  val userCount = 10
  val queryServicePath = sparkSQLHost + "/sparksql"


  val scn = scenario("Spark SQL under "+ userCount +" concurrent users")
    .exec(http("Query all data")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(spark_sql_all_data))
  )
    .pause(2)
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
    .pause(5)
    .exec(http("Query for the selection")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .queryParam("selectionOnly","true")
    .headers(json_header)
    .body(query)
    .check(bodyString.is(spark_sql_selection_data))
  )
    .pause(2)
    .exec(http("Remove the filter")
    .post(serviceRoot + "filterservice/removefilter")
    .headers(text_header)
    .body(filterId)
  )
    .pause(5)
    .exec(http("Query for filtered data")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(spark_sql_all_data))
  )
    .pause(2)
    .exec(http("Remove the selection")
    .post(serviceRoot + "selectionservice/removeselection")
    .headers(text_header)
    .body(filterId)
  )
    .pause(5)
    .exec(http("Query for selection after it was removed")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .queryParam("selectionOnly","true")
    .headers(json_header)
    .body(query)
    .check(bodyString.is(empty_data))
  )

  setUp(scn.users(userCount).ramp(5).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}