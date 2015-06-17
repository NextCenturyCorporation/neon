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
package com.ncc.neon.services

import com.ncc.neon.query.clauses.LimitClause
import com.ncc.neon.query.clauses.OffsetClause
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.Transform
import org.junit.Before
import org.junit.Test

class ExportServiceTest {

    private ExportService exportService
    private final String HOST = "localhost"
    private final String DATABASETYPE = "mongo"

    @Before
    void before() {
        exportService = new ExportService()
    }

    @Test
    void "ProcessAndExecuteQuery"() {
        HashMap<String, Object> query_fields_object = makeQueryFieldsObject()
        QueryResult result = exportService.processAndExecuteQuery(HOST, DATABASETYPE, query_fields_object)
        assert result != null
    }

    @Test
    void "createCSVForQuery"() {
        HashMap<String, Object> query_fields_object = makeQueryFieldsObject()
        File result = exportService.createCSVForQuery(query_fields_object)
        assert result.name == "timelineSelector.csv"
    }

    HashMap<String, Object> makeQueryFieldsObject() {
        HashMap<String, Object> query_fields_object = new HashMap<String, Object>()
            HashMap<String, Object> query = new HashMap<String, Object>()
                HashMap<String, Object> filter = new HashMap<String, Object>()
                filter.put("databaseName", "test")
                filter.put("tableName", "earthquakes")
                    HashMap<String, Object> whereClause = new HashMap<String, Objects>()
                    whereClause.put("type", "where")
                    whereClause.put("lhs", "time")
                    whereClause.put("operator", "!=")
                    whereClause.put("rhs", null)
                filter.put("whereClause", whereClause)
            query.put("filter", filter)
                List<Object> queryFields = []
                queryFields.push("*")
            query.put("fields", queryFields)
            query.put("ignoreFilters_", false)
            query.put("selectionOnly_", false)
                List<Object> ignoredFilterIds_ = []
                ignoredFilterIds_.push("timeline-test-earthquakes-1e8ed190-dcb4-4019-a5b2-5bcdc80b3b5d")
            query.put("ignoredFilterIds_", ignoredFilterIds_)
                List<Object> groupByClauses = []
                    HashMap<String, Object> groupByClause0 = new HashMap<String, Object>()
                    groupByClause0.put("type", "function")
                    groupByClause0.put("operation", "year")
                    groupByClause0.put("field", "time")
                    groupByClause0.put("name", "year")
                groupByClauses.push(groupByClause0)
                    HashMap<String, Object> groupByClause1 = new HashMap<String, Object>()
                    groupByClause1.put("type", "function")
                    groupByClause1.put("operation", "month")
                    groupByClause1.put("field", "time")
                    groupByClause1.put("name", "month")
                groupByClauses.push(groupByClause1)
                    HashMap<String, Object> groupByClause2 = new HashMap<String, Object>()
                    groupByClause2.put("type", "function")
                    groupByClause2.put("operation", "dayOfMonth")
                    groupByClause2.put("field", "time")
                    groupByClause2.put("name", "day")
                groupByClauses.push(groupByClause2)
            query.put("groupByClauses", groupByClauses)
            query.put("isDistinct", false)
                List<Object> aggregates = []
                    HashMap<String, Object> aggregate1 = new HashMap<String, Object>()
                    aggregate1.put("operation", "count")
                    aggregate1.put("field", "*")
                    aggregate1.put("name", "count")
                aggregates.push(aggregate1)
                    HashMap<String, Object> aggregate2 = new HashMap<String, Object>()
                    aggregate2.put("operation", "min")
                    aggregate2.put("field", "time")
                    aggregate2.put("name", "date")
                aggregates.push(aggregate2)
            query.put("aggregates", aggregates)
                List<Object> sortClauses = []
                    HashMap<String, Object> sortClause1 = new HashMap<String, Object>()
                    sortClause1.put("fieldName", "date")
                    sortClause1.put("sortOrder", 1)
                sortClauses.push(sortClause1)
            query.put("sortClauses", sortClauses)
            // =========================================================================================================
                LimitClause limitClause = new LimitClause()
            query.put("limitClause", limitClause)
                OffsetClause offsetClause = new OffsetClause()
            query.put("offsetClause", offsetClause)
                Transform[] transforms = new Transform[5]
            query.put("transforms", transforms)
            // =========================================================================================================
        query_fields_object.put("query", query)
        query_fields_object.put("name", "timelineSelector")
            List<Object> fields = []
                HashMap<String, Object> field0 = new HashMap<String, Object>()
                field0.put("query", "")
                field0.put("pretty", "")
            fields.push(field0)
                HashMap<String, Object> field1 = new HashMap<String, Object>()
                field1.put("query", "")
                field1.put("pretty", "")
            fields.push(field1)
                HashMap<String, Object> field2 = new HashMap<String, Object>()
                field2.put("query", "")
                field2.put("pretty", "")
            fields.push(field2)
                HashMap<String, Object> field3 = new HashMap<String, Object>()
                field3.put("query", "")
                field3.put("pretty", "")
            fields.push(field3)
                HashMap<String, Object> field4 = new HashMap<String, Object>()
                field4.put("query", "")
                field4.put("pretty", "")
            fields.push(field4)
        query_fields_object.put("fields", fields)
        return query_fields_object
    }

}
