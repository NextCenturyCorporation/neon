package com.ncc.neon

object Requests {

  val query = """{"filter":{"databaseName":"concurrencytest","tableName":"records"},"fields":["*"],"groupByClauses":[],"isDistinct":false,"aggregates":[],"sortClauses":[]}"""

  val add_filter = """{"id":"filterId","filter":{"whereClause":{"type":"or","whereClauses":[{"type":"where","lhs":"state","operator":"=","rhs":"VA"},{"type":"where","lhs":"state","operator":"=","rhs":"DC"}]},"databaseName":"concurrencytest","tableName":"records"}}"""

  val add_selection = """{"id":"filterId","filter":{"whereClause":{"type":"where","lhs":"salary","operator":"<=","rhs":100000},"databaseName":"concurrencytest","tableName":"records"}}"""

  val filterId = "filterId"

  val mongoHost = System.getProperty("mongo.hosts")

  val sharkHost = System.getProperty("shark.host")


}