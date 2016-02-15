package com.ncc.neon.query.elasticsearch

import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import groovy.json.JsonOutput
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Created by jwilliams on 1/27/16.
 */
@Component
class ElasticSearchHeatmapExecutor extends ElasticSearchQueryExecutor{
    final int gridCount = 10

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchHeatmapExecutor)

    QueryResult execute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }

        QueryResult result = doExecute(query, options, boundingBox)
        return transform(query.transforms, result)
    }

    QueryResult doExecute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        long d1 = new Date().getTime()

        ElasticSearchHeatmapConversionStrategy conversionStrategy = new ElasticSearchHeatmapConversionStrategy(filterState: filterState, selectionState: selectionState)

        query.groupByClauses = []

        def request = conversionStrategy.convertQuery(query, options, boundingBox)

        def aggregates = query.aggregates
        def groupByClauses = query.groupByClauses

        def results = getClient().search(request).actionGet()
        def aggResults = results.aggregations

        def returnVal
        if (aggregates && !groupByClauses) {
            returnVal = new TabularQueryResult([
                    extractMetrics(aggregates, aggResults ? aggResults.asMap() : null, results.hits.totalHits)
            ])
        } else if (groupByClauses) {
            List<Map<String, Object>> buckets = extractBuckets(groupByClauses, aggResults.asList()[0], aggregates)
            buckets = limitBuckets(buckets, query)
            returnVal = new TabularQueryResult(buckets)
        } else if(query.isDistinct) {
            returnVal = new TabularQueryResult(extractDistinct(query, aggResults.asList()[0]))
        }
        else {
            returnVal = new TabularQueryResult(results.hits.collect { it.getSource() })
        }

        long diffTime = new Date().getTime() - d1
        LOGGER.debug(" Query took: " + diffTime + " ms ")

        return returnVal
    }
}
