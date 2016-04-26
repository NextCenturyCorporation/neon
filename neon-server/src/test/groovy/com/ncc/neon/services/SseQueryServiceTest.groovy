package com.ncc.neon.services

import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.AggregateClause
import com.ncc.neon.query.executor.QueryExecutor
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.GlobalFilterState
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.sse.MongoRecordCounter
import com.ncc.neon.sse.RecordCounter
import com.ncc.neon.sse.RecordCounterFactory
import groovy.json.JsonSlurper
import org.glassfish.jersey.media.sse.EventOutput
import org.junit.Before
import org.junit.Test

/**
 * Created by cdorman on 3/17/16.
 */
public class SseQueryServiceTest {

    private static final String HOST = "aHost"
    private static final String DATABASE_TYPE = DataSources.mongo.toString()
    protected static final String DATABASE_NAME = "database"
    protected static final String TABLE_NAME = "table"

    private QueryService queryService
    private Filter simpleFilter
    protected Query simpleQuery
    List<AggregateClause> aggregates
    protected RecordCounterFactory recordCounterFactory
    protected RecordCounter recordCounter

    GlobalFilterState filterState

    @Before
    void before() {
        queryService = new QueryService()
        QueryExecutor executor = [execute: { query, options, filterState -> new TabularQueryResult([["key1": 4], ["key2": 2]]) }] as QueryExecutor
        queryService.filterState = new GlobalFilterState()
        queryService.queryExecutorFactory = [getExecutor: { connection ->
            assert connection.host == HOST
            assert connection.dataSource == DataSources.valueOf(DATABASE_TYPE)
            executor
        }] as QueryExecutorFactory

        recordCounter = [getCount: { String a, String b, String c -> 123235l }] as RecordCounter
        recordCounterFactory = [getRecordCounter: { recordCounter }] as RecordCounterFactory

        simpleFilter = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME)
        AggregateClause ac = new AggregateClause()
        ac.name = "key1"
        ac.operation = "count"
        aggregates = [ac]
        simpleQuery = new Query(filter: simpleFilter, aggregates: aggregates)

        filterState = new GlobalFilterState()
    }

    @Test
    public void testStoreQueryGroupData() throws Exception {
        SseQueryService sqs = new SseQueryService()
        sqs.queryExecutorFactory = queryService.queryExecutorFactory
        sqs.recordCounterFactory = recordCounterFactory
        sqs.filterState = filterState

        def response = sqs.storeQueryData(HOST, DATABASE_TYPE, false, false, new HashSet(), simpleQuery)
        def uuid = new JsonSlurper().parseText(response.entity).uuid

        EventOutput eo = sqs.executeByUuid(uuid)
    }
}