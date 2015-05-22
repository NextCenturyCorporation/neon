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
import com.ncc.neon.query.sparksql.SparkSQLConversionStrategy
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
		// TODO: Have mechansism to specify which schema file to use
		props.put("model", CalciteQueryExecutor.class.getResource("/mongo-earthquakes-model.json").getPath());
		Connection connection =
		DriverManager.getConnection("jdbc:calcite:", props);
        SparkSQLConversionStrategy conversionStrategy = new SparkSQLConversionStrategy(filterState: filterState, selectionState: selectionState)
        String jdbcQuery = conversionStrategy.convertQuery(query, options)
		// Should be: "select id, magnitude, place from earthquakes where magnitude > 6"
		System.err.println("Query = " + jdbcQuery);
		ResultSet resultSet = connection.createStatement().executeQuery(jdbcQuery);
        QueryResult results = convertResults(resultSet);
		return results;
    }

    @Override
    List<String> showDatabases() {
        LOGGER.info("Executing getDatabaseNames")
		System.err.println("Executing getDatabaseNames");

		Properties props = new Properties();
		// TODO: Have mechansism to specify which schema file to use
		props.put("model", CalciteQueryExecutor.class.getResource("/mongo-earthquakes-model.json").getPath());
		Connection connection =	DriverManager.getConnection("jdbc:calcite:", props)
		DatabaseMetaData metadata = connection.getMetaData();
		ResultSet results = metadata.getSchemas();
		List<String> resultList = getField("TABLE_SCHEM", results);
		
		return resultList;
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.info("Executing getCollectionNames on database {}", dbName)
		System.err.println("Executing getCollectionNames on database " + dbName);
		
		// Calcite does some weird capitalization stuff, and haven't figured it out totally.
		// In the mean time, shove evertything uppercase
		dbName = dbName.toUpperCase();

		Properties props = new Properties();
		// TODO: Have mechansism to specify which schema file to use
		props.put("model", CalciteQueryExecutor.class.getResource("/mongo-earthquakes-model.json").getPath());
		Connection connection =	DriverManager.getConnection("jdbc:calcite:", props)
		DatabaseMetaData metadata = connection.getMetaData();
		ResultSet results = metadata.getTables(null, dbName, null, null);
		List<String> resultList = getField("TABLE_NAME", results);
		
		return resultList;
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        LOGGER.info("Executing getFieldNames on table {}", tableName)
		System.err.println("Executing getFieldNames on table " + tableName);

		// Calcite does some weird capitalization stuff, and haven't figured it out totally.
		// In the mean time, shove evertything uppercase
		databaseName = databaseName.toUpperCase();
		tableName = tableName.toUpperCase();
		
		Properties props = new Properties();
		// TODO: Have mechansism to specify which schema file to use
		props.put("model", CalciteQueryExecutor.class.getResource("/mongo-earthquakes-model.json").getPath());
		Connection connection =	DriverManager.getConnection("jdbc:calcite:", props)
		DatabaseMetaData metadata = connection.getMetaData();
		ResultSet results = metadata.getColumns(null, databaseName, tableName, null);
		List<String> resultList = getField("COLUMN_NAME", results);
				
		return resultList;
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

	/** Pull a single field from the ResultSet into a list.
	 */
	static ArrayList<String> getField(String fieldName, ResultSet resultSet) {
		List<String> stringList = new ArrayList<String>();
		final ResultSetMetaData metaData = resultSet.getMetaData();
		while (resultSet.next()) {
			stringList.add(resultSet.getString(fieldName));
		}
		return stringList;
	}

	/** Convenience method for shoving a result in a ResultSet into a single string you can easily look at.
	 */
	static ArrayList<String> prettyStringifyResults(ResultSet resultSet) {
		List<String> stringList = new ArrayList<String>();
		final ResultSetMetaData metaData = resultSet.getMetaData();
		while (resultSet.next()) {
			int n = metaData.getColumnCount();
			if (n > 0) {
				String str = "[";
				for (int i = 1; i <= n; i++) {
					str = str + metaData.getColumnLabel(i) + "=" + resultSet.getString(i) + ",";
				}
				str = str.substring(0, str.length()-1) + "]";
				stringList.add(str);
			}
		}
		return stringList;
	}

}
