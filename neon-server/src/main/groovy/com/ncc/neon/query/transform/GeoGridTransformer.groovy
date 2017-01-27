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
			Set boxes = determineBoxes(getFieldValue(point, params.latField), getFieldValue(point, params.lonField), params.maxLat, params.minLon, boxWidth, boxHeight)
			boxes.each { box ->
				if(box.horizBox < 0 || box.horizBox >= numHorizTiles || box.vertBox < 0 || box.vertBox >= numVertTiles) {
					return
				}
				def addTo = buckets[box.horizBox][box.vertBox].data.find { getFieldValue(it, params.aggregationField) == pointAgg }
				if(addTo) {
					addTo.count += 1
				}
				else {
					buckets[box.horizBox][box.vertBox].data << [count: 1, (params.aggregationField): pointAgg]
				}
			}
		}
		List<Map<String, Object>> newData = []
		for(int x = 0; x < numHorizTiles; x++) {
			for(int y = 0; y < numVertTiles; y++) {
				newData << buckets[x][y]
			}
		}
		List toReturn = [[data: queryResult.getData()], [data: newData]]
		return new TabularQueryResult(toReturn)
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

	private Set determineBoxes(def latValue, def lonValue, double maxLat, double minLon, double boxWidth, double boxHeight) {
		if(latValue instanceof Number || latValue instanceof String) {
			return [
				[
					horizBox: (((lonValue as double) - minLon) / boxWidth) as int,
					vertBox: ((maxLat - (latValue as double)) / boxHeight) as int
				]
			]
		}
		// If latValue and lonValue aren't numbers or strings, assume they're lists of some description.
		if(!latValue || !lonValue) {
			return []
		}
		// Zip latValue and lonValue into something of the form [[lat1, lon1]. [lat2, lon2], etc], and then
		// transform each [latX, lonX] pair into a map of the form [horizBox: ___, vertBox: ___]
		List latLonPairs = GroovyCollections.transpose([latValue, lonValue])
		latLonPairs = latLonPairs.collect {
			[
				horizBox: (((it[1] as double) - minLon) / boxWidth) as int,
				vertBox: ((maxLat - (it[0] as double)) / boxHeight) as int
			]
		}
		return latLonPairs as Set // Convert to Set to remove duplicate boxes
	}
}