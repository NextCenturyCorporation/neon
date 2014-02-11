package com.ncc.neon

object Requests {

  val filter_key_request = """{"databaseName":"concurrencytest","tableName":"records"}"""

  val all_data_query = """{"filter":{"databaseName":"concurrencytest","tableName":"records"},"fields":["*"],"disregardFilters_":true,"selectionOnly_":false,"groupByClauses":[],"isDistinct":false,"aggregates":[],"sortClauses":[]}"""

  val add_filter = """{"filterKey":{"uuid":"8ed4a02e-53fa-4c41-850f-0af77cfbeb81","dataSet":{"databaseName":"concurrencytest","tableName":"records"}},"filter":{"whereClause":{"type":"or","whereClauses":[{"type":"where","lhs":"state","operator":"=","rhs":"VA"},{"type":"where","lhs":"state","operator":"=","rhs":"DC"}]},"databaseName":"concurrencytest","tableName":"records"}}"""

  val add_selection = """{"filterKey":{"uuid":"8ed4a02e-53fa-4c41-850f-0af77cfbeb81","dataSet":{"databaseName":"concurrencytest","tableName":"records"}},"filter":{"whereClause":{"type":"where","lhs":"salary","operator":"<=","rhs":100000},"databaseName":"concurrencytest","tableName":"records"}}"""

  val selection_only_query =  """{"filter":{"databaseName":"concurrencytest","tableName":"records"},"fields":["*"],"disregardFilters_":false,"selectionOnly_":true,"groupByClauses":[],"isDistinct":false,"aggregates":[],"sortClauses":[]}"""

  val filtered_query = """{"filter":{"databaseName":"concurrencytest","tableName":"records"},"fields":["*"],"disregardFilters_":false,"selectionOnly_":false,"groupByClauses":[],"isDistinct":false,"aggregates":[],"sortClauses":[]}"""

  val filter_key = """{"uuid":"8ed4a02e-53fa-4c41-850f-0af77cfbeb81","dataSet":{"databaseName":"concurrencytest","tableName":"records"}}"""

  val mongoHost = System.getProperty("mongo.hosts")
  val mongo_connection_request = """{"dataSource":"mongo","connectionUrl":"""" + mongoHost +""""}"""

  val hiveHost = System.getProperty("hive.host")
  val hive_connection_request = """{"dataSource":"hive","connectionUrl":"""" + hiveHost +""""}"""

}