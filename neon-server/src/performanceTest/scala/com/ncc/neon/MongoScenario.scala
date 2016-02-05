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


class MongoScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val serviceRoot = "/neon/services/"
  val userCount = 12
  val queryServicePath = mongoHost + "/mongo"

  val scn = scenario("Mongo under " + userCount + " concurrent users")
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
    .exec(http("Query with filters")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_filtered_data))
  )
    .pause(1)
    .exec(http("Query ignore filters")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .queryParam("ignoreFilters","true")
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_all_data))
  )

    .pause(1)
    .exec(http("Query for selection")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .queryParam("selectionOnly","true")
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_selection_data))
  )
    .pause(1)
    .exec(http("Remove selection")
    .post(serviceRoot + "selectionservice/removeselection")
    .headers(text_header)
    .body(filterId)
  )
    .pause(2)
    .exec(http("Remove filter")
    .post(serviceRoot + "filterservice/removefilter")
    .headers(text_header)
    .body(filterId)
  )
    .pause(2)
    .exec(http("Query for selection after it was removed")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .headers(json_header)
    .queryParam("selectionOnly","true")
    .body(query)
    .check(bodyString.is(empty_data))
  )
    .pause(1)
    .exec(http("Query after the filter was removed")
    .post(serviceRoot + "queryservice/query/" + queryServicePath)
    .headers(json_header)
    .body(query)
    .check(bodyString.is(mongo_all_data))
  )

  setUp(scn.users(userCount).ramp(3).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}