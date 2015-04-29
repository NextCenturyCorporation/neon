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

package com.ncc.neon.connect

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.*

import groovy.json.JsonSlurper
import groovy.json.JsonException

@SuppressWarnings("UnusedImport")
import groovy.json.internal.Exceptions$JsonInternalException

/**
 * Wrapper for JDBC API
 */
class JdbcClient implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcClient)

    /** used to match the limit clause */
    private static final def LIMIT_REGEX = /(?i)(LIMIT\s+)(\d+)/

    private Connection connection

    JdbcClient(Connection connection) {
        this.connection = connection
    }

    /**
     * Executes the specified query
     * @param offset An optional number of rows to skip in the result set. This is provided as a parameter to
     * execute query since not all JDBC drivers (namely Spark SQL) support doing this as part of the query
     */
    List executeQuery(String query, int offset = 0) {
        Statement statement
        ResultSet resultSet
        try {
            statement = connection.createStatement()
            resultSet = statement.executeQuery(addOffsetToLimitQuery(query, offset))
            return createMappedValuesFromResultSet(resultSet, offset)
        }
        finally {
            resultSet?.close()
            statement?.close()
        }

        return []
    }

    /**
     * When an offset is specified and a limit is used, we need to return the offset plus the limit because
     * we're doing the offset in memory. This modifies the query string to do that.
     * @param query
     * @param offset
     * @return
     */
    private static String addOffsetToLimitQuery(String query, int offset) {
        if (offset > 0) {
            // replace the limit X clause with limit X+offset so we can still return the correct number
            // of records
            def matcher = (query =~ LIMIT_REGEX)
            if (matcher) {
                int limit = matcher[0][2].toInteger()
                String modified = matcher.replaceAll("${matcher[0][1]}${limit + offset}")
                return modified
            }
        }
        return query

    }

    private List createMappedValuesFromResultSet(ResultSet resultSet, int offset) {
        List resultList = []
        ResultSetMetaData metadata = resultSet.metaData
        int columnCount = metadata.columnCount
        if (moveCursor(resultSet, offset)) {
            while (resultSet.next()) {
                def result = [:]
                for (ii in 1..columnCount) {
                    String columnName = metadata.getColumnName(ii)
                    result[columnName] = getValue(metadata, resultSet, ii)
                }
                resultList.add(result)
            }
        }
        return resultList
    }

    /**
     * Moves the cursor in the result set by the specified offset
     * @param resultSet The result set whose cursor is being advanced - this may be modified
     * @param offset The number of records to offset it by
     * @return true if there are still potentially more records available after the cursor is moved, false if not. This
     * value may differ from resultSet.next() if an offset larger than the number of records is provided - the
     * cursor may not actually be advanced to save time. When this method returns true, use next() to determine if there
     * are more records to iterate over. When this method returns false, assume there are no more records.
     */
    @SuppressWarnings("CatchException")
    @SuppressWarnings("MethodSize") // there are only a couple of lines of code in here, but a large comment block explaining why we implemented this how we did
    private static boolean moveCursor(ResultSet resultSet, int offset) {
        if (offset > 0) {
            try {
                // if the call to "absolute" succeeds, there are still results left
                // if it does not succeed, it means we went past the end of the result set (or before the beginning
                // which wouldn't make sense in this case). When this happens, there are no more results to iterate
                // over, but rather than actually iterating through the result set return false indicating that so
                // the result set is ignored (and "last" is not supported all jdbc drivers, Spark SQL for example)
                return resultSet.absolute(offset)
            }
            // happens if this is not supported, but implementations may throw different types of exceptions so
            // catch all of them
            catch (Exception e) {
                LOGGER.debug("ResultSet absolute call failed with exception {}. Using manual offset.", e)
                advanceResultSet(resultSet, offset)

                // always return true from here even if the result set is actually at the end. next() will return
                // false in the cases where there are no results left and will not take any real time to do so
                return true
            }
        }
        // no offset
        return true
    }

    private static void advanceResultSet(ResultSet resultSet, int offset) {
        int count = 0
        while (count < offset && resultSet.next()) {
            count++
        }
    }

    @SuppressWarnings('EmptyCatchBlock')
    private def getValue(ResultSetMetaData metadata, ResultSet resultSet, int index) {
        def val = resultSet.getObject(index)

        // timestamps are time-zone less, but we assume UTC
        if(val && metadata.getColumnType(index) == Types.TIMESTAMP) {
            // use joda time because not all jdbc drivers (e.g. Spark SQL) support timezones - they return in local time
            val = new DateTime(val.time).withZoneRetainFields(DateTimeZone.UTC).toDate()
        } else if(val && metadata.getColumnType(index) == Types.VARCHAR && val[0] == "[") {
            try {
                val = new JsonSlurper().parseText(val)
            } catch(JsonException jsonE) {
                //Do nothing -- don't touch the val if it is not json
            } catch(Exceptions$JsonInternalException internalJsonE) {
                //Do nothing -- don't touch the val if it is not json
            }
        }
        return val
    }

    /**
     * Each jdbcClient instance is created per session,
     */
    void execute(String query) {
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }

    List<String> getColumnNames(String databaseName, String tableName) {
        def columns = []
        ResultSet rs = connection.metaData.getColumns(null, databaseName, tableName, null)

        while (rs.next()) {
            // column 4 is the column name
            columns << rs.getString(4)
        }
        return columns
    }

    @Override
    void close() {
        connection?.close()
        connection = null
    }

}
