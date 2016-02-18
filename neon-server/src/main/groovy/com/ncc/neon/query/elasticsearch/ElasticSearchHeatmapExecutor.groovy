package com.ncc.neon.query.elasticsearch

import com.mongodb.util.JSON
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchHeatmapExecutor)

    QueryResult execute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }

        QueryResult result = doExecute(query, options, boundingBox)
        return result
    }

    QueryResult doExecute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        long d1 = new Date().getTime()

        ElasticSearchHeatmapConversionStrategy conversionStrategy = new ElasticSearchHeatmapConversionStrategy(filterState: filterState, selectionState: selectionState)

        query.groupByClauses = []

        def request = conversionStrategy.convertQuery(query, options, boundingBox)

        def results = getClient().search(request).actionGet()
        def aggResults = results.aggregations

        def returnVal

        List<Map<String, Object>> buckets = extractHeatmapBuckets(aggResults.asList()[0].aggregations[0])
        returnVal = new TabularQueryResult(buckets)

        long diffTime = new Date().getTime() - d1
        LOGGER.debug(" Query took: " + diffTime + " ms ")

        return returnVal
    }

    private List<Map<String, Object>> extractHeatmapBuckets(def gridAggregation) {
        List bucketList = gridAggregation.buckets;

        List<Map<String, Object>> buckets = new ArrayList<Map<String, Object>>()

        def maxCount = 0
        bucketList.each {
            if(it.docCount > maxCount) {
                maxCount = it.docCount
            }
        }

        bucketList.each {
            def bucket = [:]
            def point = [:]
            point['lat'] = it.keyAsGeoPoint.lat
            point['lon'] = it.keyAsGeoPoint.lon
            bucket['point'] = point
            bucket['count'] = it.docCount
            bucket['percentage'] = (it.docCount / maxCount)
            buckets.add(bucket)
        }

        return buckets
    }
}
