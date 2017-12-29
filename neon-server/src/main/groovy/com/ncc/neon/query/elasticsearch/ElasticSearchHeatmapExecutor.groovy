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

import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Component

@Component("elasticSearchHeatmapExecutor")
class ElasticSearchHeatmapExecutor extends ElasticSearchRestQueryExecutor{
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
        List bucketList = gridAggregation.buckets

        List<Map<String, Object>> buckets = []

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
            bucket['percentage'] = maxCount > 0 ? (it.docCount / maxCount) : 0
            buckets.add(bucket)
        }

        return buckets
    }
}
