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

package com.ncc.neon.query.elasticsearch

import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.Client
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.util.ResourceNotFoundException

@Primary
@Component("elasticSearchRestQueryExecutor")
class ElasticSearchRestQueryExecutor extends AbstractQueryExecutor {

	static final String STATS_AGG_PREFIX = "_statsFor_"

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRestQueryExecutor)

	/*
	 * A small class to hold the important information about an aggregation bucket between when the
	 * buckets are taken out of ElasticSearch's hierarchical arrangement and when everything can be
	 * extracted into the format that the Neon API uses.
	 */
	private static class AggregationBucket {
		def getGroupByKeys() {
			return groupByKeys
		}

		def setGroupByKeys(newGroupByKeys) {
			groupByKeys = newGroupByKeys
		}

		def getAggregatedValues() {
			return aggregatedValues
		}

		def setAggregatedValues(newValues) {
			aggregatedValues = newValues
		}

		def getDocCount() {
			return docCount
		}

		def setDocCount(newCount) {
			docCount = newCount
		}

		private Map groupByKeys
		private Map aggregatedValues = [:]
		private def docCount
	}

	@Autowired
	protected FilterState filterState

	@Autowired
	protected SelectionState selectionState

	@Autowired
	protected ConnectionManager connectionManager

	@Override
	QueryResult doExecute(Query query, QueryOptions options) {
		returnVal = new TabularQueryResult()
		return returnVal
	}


	@Override
	List<String> showDatabases() {
		LOGGER.debug("Executing showDatabases to retrieve indices")
		getClient().admin().cluster().state(new ClusterStateRequest()).actionGet().state.metaData.indices.collect { it.key }
	}

	@Override
	List<String> showTables(String dbName) {
		LOGGER.debug("Executing showTables for index " + dbName + " to get type mappings")
		// Elastic search allows wildcards in index names.  If dbName contains a wildcard, find all matches.
		if (dbName?.contains('*')) {
			def match = dbName.replaceAll(/\*/, '.*')
			List<String> tables = []
			def mappings = getMappings()
			mappings.keysIt().each {
				if (it.matches(match)) {
					tables.addAll(mappings.get(it).collect{ table -> table.key })
				}
			}
			return tables.unique()
		}

		def dbExists = getClient().admin().indices().exists(new IndicesExistsRequest(dbName)).actionGet().isExists()
		if(!dbExists) {
			throw new ResourceNotFoundException("Database ${dbName} does not exist")
		}

		// Fall through case is to return the exact match.
		getMappings().get(dbName).collect { it.key }
	}

	@Override
	List<String> getFieldNames(String databaseName, String tableName) {
		if(databaseName && tableName) {
			def dbMatch = databaseName.replaceAll(/\*/, '.*')
			def tableMatch = tableName.replaceAll(/\*/, '.*')

			def fields = []
			def mappings = getMappings()
			mappings.keysIt().each { dbKey ->
				if (dbKey.matches(dbMatch)) {
					def dbMappings = mappings.get(dbKey)
					dbMappings.keysIt().each { tableKey ->
						if (tableKey.matches(tableMatch)) {
							def tableMappings = dbMappings.get(tableKey)
							fields.addAll(getFieldsInObject(tableMappings.getSourceAsMap(), null))
						}
					}
				}
			}

			if (fields) {
				fields.add("_id")
				return fields.unique()
			}
		}
		throw new ResourceNotFoundException("Fields for Database ${databaseName} and Table ${tableName} do not exist")
	}

	@Override
	Map getFieldTypes(String databaseName, String tableName) {
		def fieldTypes = [:]
		if(databaseName && tableName) {
			def dbMatch = databaseName.replaceAll(/\*/, '.*')
			def tableMatch = tableName.replaceAll(/\*/, '.*')
			def mappings = getMappings()
			mappings.keysIt().each { dbKey ->
				if (dbKey.matches(dbMatch)) {
					def dbMappings = mappings.get(dbKey)
					dbMappings.keysIt().each { tableKey ->
						if (tableKey.matches(tableMatch)) {
							def tableMappings = dbMappings.get(tableKey)
							fieldTypes.putAll(getFieldTypesInObject(tableMappings.getSourceAsMap(), null))
						}
					}
				}
			}
		}

		return fieldTypes
	}

	protected Client getClient() {
		return connectionManager.connection.client
	}

}
