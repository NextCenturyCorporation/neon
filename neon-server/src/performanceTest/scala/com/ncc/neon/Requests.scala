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

object Requests {

  val query = """{"filter":{"databaseName":"concurrencytest","tableName":"records"},"fields":["*"],"groupByClauses":[],"isDistinct":false,"aggregates":[],"sortClauses":[]}"""

  val add_filter = """{"id":"filterId","filter":{"whereClause":{"type":"or","whereClauses":[{"type":"where","lhs":"state","operator":"=","rhs":"VA"},{"type":"where","lhs":"state","operator":"=","rhs":"DC"}]},"databaseName":"concurrencytest","tableName":"records"}}"""

  val add_selection = """{"id":"filterId","filter":{"whereClause":{"type":"where","lhs":"salary","operator":"<=","rhs":100000},"databaseName":"concurrencytest","tableName":"records"}}"""

  val filterId = "filterId"

  val mongoHost = System.getProperty("mongo.host")

  val sparkSQLHost = System.getProperty("sparksql.host")


}