/*
 * Copyright 2013 Next Century Corporation
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

package com.ncc.neon.query.calcite
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
/**
 * Executes queries against a calcite-compliant data store
 */
@Component
class CalciteQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalciteQueryExecutor)

    private static final GET_FIELD_NAMES_LIMIT = 1000

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
		LOGGER.info("Executing doExecute");
		System.err.println("Executing doExecute");
		
		Properties props = new Properties();
		props.put("model", CalciteQueryExecutor.class.getResource("/mongo-earthquakes-model.json").getPath());
		Connection connection =
		DriverManager.getConnection("jdbc:calcite:", props);
		ResultSet resultSet = connection.createStatement().executeQuery(
				"select id, magnitude, place from earthquakes where magnitude > 6");

        QueryResult results = convertResults(resultSet);
		return results;
    }

    @Override
    List<String> showDatabases() {
        LOGGER.info("Executing getDatabaseNames")
		System.err.println("Executing getDatabaseNames");
        return new ArrayList<String>();
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.info("Executing getCollectionNames on database {}", dbName)
		System.err.println("Executing getCollectionNames on database {}", dbName);
        return new ArrayList<String>()
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        LOGGER.info("Executing getFieldNames on table {}", tableName)
		System.err.println("Executing getFieldNames on table {}", tableName);
        return new ArrayList<String>()
    }
	
	/** Copy a SQL ResultSet into a Neon QueryResult
	 */
	static QueryResult convertResults(ResultSet resultSet) {
		List<Map<String,Object>> resultTable = new ArrayList<Map<String, Object>>();
		final ResultSetMetaData metaData = resultSet.getMetaData();
		while (resultSet.next()) {
			int n = metaData.getColumnCount();
			if (n > 0) {
				Map<String, Object> rowMap = new HashMap();
				for (int i = 1; i <= n; i++) {
					rowMap.put(metaData.getColumnLabel(i), resultSet.getString(i));
				}
				resultTable.add(rowMap);
			}
		}
		QueryResult converted = new TabularQueryResult(resultTable);
		return converted;
	}

}
