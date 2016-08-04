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

class GeoGridTransformer implements Transformer {

	    /**
     * Expects params to be an object of the form {
     *       latField: string, giving the name of the latitude field in returned records,
     *       lonField: string, giving the name of the longitude field in returned records,
     *       aggregationField: string, giving the name of the field to aggregate on,
     *       minLat: double, gives the minimum latitude value on the map,
     *       maxLat: double, gives the maximum latitude on the map,
     *       minLon: double, gives the minimum longitude on the map,
     *       maxLon: double, gives the maximum longitude on the map,
     *       numTilesVertical: integer, gives the number of tiles to fit vertically on the map,
     *       numTilesHorizontal: integer, gives the number of tiles to fit horizontally on the map
     *   }
     */
	@Override
	QueryResult convert(QueryResult queryResult, def params) {
		String aggField = params.aggregationField
		Map<String, Object>[][] buckets = new Map<String, Object>[params.numTilesHorizontal][params.numTilesVertical]
		for(int x = 0; x < params.numTilesHorizontal; x++) {
			for(int y = 0; y < params.numTilesVertical; y++) {
				buckets[x][y] = [
					left: params.minLon + (x / params.numTilesHorizontal) * Math.abs(params.maxLon - params.minLon),
					right: params.minLon + ((x + 1) / params.numTilesHorizontal) * Math.abs(params.maxLon - params.minLon),
					top: params.maxLat - (y / params.numTilesVertical) * Math.abs(params.maxLat - params.minLat),
					bottom: params.maxLat - ((y + 1) / params.numTilesVertical) * Math.abs(params.maxLat - params.minLat),
					data: []
				]
			}
		}
		double boxWidth = Math.abs(params.maxLon - params.minLon) / params.numTilesHorizontal
		double boxHeight = Math.abs(params.maxLat - params.minLat) / params.numTilesVertical
		queryResult.getData().each { point ->
			int horizBox = ((point[params.lonField] - params.minLon) / boxWidth) as int
			int vertBox = ((params.maxLat - point[params.latField]) / boxHeight) as int
			if(horizBox >= params.numTilesHorizontal || horizBox < 0 || vertBox >= params.numTilesVertical || vertBox < 0) {
				return
			}
			def addTo = buckets[horizBox][vertBox].data.find { it[aggField] == point[aggField] }
			if(addTo) {
				addTo.count += 1
			}
			else {
				buckets[horizBox][vertBox].data << [count: 1, (aggField): point[aggField]]
			}
		}
		List<Map<String, Object>> newData = []
		for(int x = 0; x < params.numTilesHorizontal; x++) {
			for(int y = 0; y < params.numTilesVertical; y++) {
				newData << buckets[x][y]
			}
		}
		return new TabularQueryResult(newData)
	}

	@Override
	String getName() {
		return GeoGridTransformer.name
	}
}