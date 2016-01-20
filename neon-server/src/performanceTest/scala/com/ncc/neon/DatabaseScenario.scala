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
import com.excilys.ebi.gatling.http.Headers._
import akka.util.duration._
import bootstrap._
import assertions._
import Headers._
import Requests._

class DatabaseScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://localhost:11402")
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0")

  val serviceRoot = "/neon/services/"

  val scn = scenario("Query for database and table names")
    .exec(http("Get Database Names")
    .get("http://localhost:11402/neon/services/queryservice/databasenames/" + mongoHost + "/mongo")
    .check(jsonPath("$[?(@=='concurrencytest')]").exists))

    .exec(http("Get Table Names")
    .get("http://localhost:11402/neon/services/queryservice/tablenames/" + mongoHost + "/mongo/concurrencytest")
    .headers(form_header)
    .check(jsonPath("$[?(@=='records')]").exists))


  setUp(scn.users(2).protocolConfig(httpConf))
  assertThat(global.failedRequests.count.is(0))
}