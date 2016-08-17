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
		int numHorizTiles = Math.round(params.numTilesHorizontal)
		int numVertTiles = Math.round(params.numTilesVertical)
		double boxWidth = Math.abs(params.maxLon - params.minLon) / numHorizTiles
		double boxHeight = Math.abs(params.maxLat - params.minLat) / numVertTiles
		Map<String, Object>[][] buckets = makeBuckets(params.minLat, params.maxLat, params.minLon, params.maxLon, numHorizTiles, numVertTiles)

		queryResult.getData().each { point ->
			def pointAgg = getFieldValue(point, params.aggregationField)
			int horizBox = (((getFieldValue(point, params.lonField) as int) - params.minLon) / boxWidth) as int
			int vertBox = ((params.maxLat - (getFieldValue(point, params.latField) as int)) / boxHeight) as int
			if(horizBox >= numHorizTiles || horizBox < 0 || vertBox >= numVertTiles || vertBox < 0) {
				return
			}
			def addTo = buckets[horizBox][vertBox].data.find { getFieldValue(it, params.aggregationField) == pointAgg }
			if(addTo) {
				addTo.count += 1
			}
			else {
				buckets[horizBox][vertBox].data << [count: 1, (params.aggregationField): pointAgg]
			}
		}
		List<Map<String, Object>> newData = []
		for(int x = 0; x < numHorizTiles; x++) {
			for(int y = 0; y < numVertTiles; y++) {
				newData << buckets[x][y]
			}
		}
		return new TabularQueryResult(newData)
	}

	@Override
	String getName() {
		return GeoGridTransformer.name
	}

	private Map<String, Object>[][] makeBuckets(double minLat, double maxLat, double minLon, double maxLon, double numHorizTiles, double numVertTiles) {
		Map<String, Object>[][] buckets = new Map<String, Object>[numHorizTiles][numVertTiles]
		for(int x = 0; x < numHorizTiles; x++) {
			for(int y = 0; y < numVertTiles; y++) {
				buckets[x][y] = [
					left: minLon + (x / numHorizTiles) * Math.abs(maxLon - minLon),
					right: minLon + ((x + 1) / numHorizTiles) * Math.abs(maxLon - minLon),
					top: maxLat - (y / numVertTiles) * Math.abs(maxLat - minLat),
					bottom: maxLat - ((y + 1) / numVertTiles) * Math.abs(maxLat - minLat),
					data: []
				]
			}
		}
		return buckets
	}

	private def getFieldValue(def point, String fieldName) {
		if(point[fieldName] != null) {
			return point[fieldName]
		}
		List pieces = fieldName.split('\\.')
		def currentObject = point
		while(pieces.size() > 0 && currentObject != null) {
			currentObject = currentObject[pieces.remove(0)]
		}
		return currentObject
	}
}