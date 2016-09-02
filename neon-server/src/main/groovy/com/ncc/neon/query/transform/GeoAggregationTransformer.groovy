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

package com.ncc.neon.query.transform

import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.result.Transformer

/**
 * Aggregates geolocational data points, pulling nearby points into groups wit hmagnitude equal to the average
 * of their constituent magnitudes and a 'number' field that tells the number of points that group contains.
 */
class GeoAggregationTransformer implements Transformer {

    /**
     * Expects params to be an object of the form {
     *       consumeWithin: double, giving radius of points to be grouped - in terms of lat/lon degrees,
     *       latField: string, giving the name of the latitude field in returned records,
     *       lonField: string, giving the name of the longitude field in returned records,
     *       sizeField: string, giving the name of the size field in returned records (optional)
     *   }
     */
    @Override
    QueryResult convert(QueryResult queryResult, def params) {
        String lat = params.latField
        String lon = params.lonField
        String size = params.sizeField
        // Add support for locationField as well.
        List<Map<String, Object>> data = queryResult.data
        List newData = []
        data.each { point ->
            def consuming = newData.find { Math.sqrt((it[lat] - point[lat])**2 + (it[lon] - point[lon])**2) <= params.consumeWithin }
            if(consuming) {
                consuming.number += 1
                consuming[(lat)] = (consuming.number - 1) * (consuming[(lat)] / consuming.number) +
                    point[(lat)] / consuming.number
                consuming[(lon)] = (consuming.number - 1) * (consuming[(lon)] / consuming.number) +
                    point[(lon)] / consuming.number
                if(size) {
                    consuming[(size)] = (consuming.number - 1) * (consuming[(size)] / consuming.number) +
                        (point[(size)] / consuming.number)
                }
            }
            else {
                point.number = 1
                newData << point
            }
        }
        return new TabularQueryResult(newData)
    }

    @Override
    String getName() {
        return GeoAggregationTransformer.name
    }
}