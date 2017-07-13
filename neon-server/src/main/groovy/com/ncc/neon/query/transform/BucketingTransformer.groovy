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

import bucketing.BucketingSystem
import bucketing.Bucket

class BucketingTransformer implements Transformer {

	// QuadNode quadTree;
	BucketingSystem bucketor;
	def logFile;
	def querySize = 0;
	def aggFieldValue;
	// String s = null;
	// int i = 1;
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
	QueryResult convert(QueryResult queryResult, def params) {//*
		// logFile = new File('dxcvLog.txt')
		// logFile << "\$" + String.valueOf(params.loadNew) + '\n'
		// logFile << String.valueOf(params.intervals) + '\n'
		// logFile << String.valueOf(params.intervals[0].getClass()) + '\n'
		// logFile << String.valueOf(params.intervals[params.intervals.size()-1].getClass()) + '\n'

		// def newQuerySize = queryResult.getData().size()
		// if (newQuerySize != querySize && queryResult.getData() != null) {
			// logFile << querySize + " " + newQuerySize + "\n"
			// def querySize = newQuerySize

		// note: despite the check below, if loadNew is false, then bucketor should not be null
		if ( (params.loadNew || bucketor == null) && queryResult != null && queryResult.getData() != null) {

			// logFile << "\$" + String.valueOf(params.intervals) + '\n'
			for (int i = 0; i < params.intervals.size(); i++) {
				// logFile << "\n" + String.valueOf("intClassBefore: " + params.intervals[i].getClass()) + '\n'
				params.intervals[i] = (double)(params.intervals[i])
				// logFile << String.valueOf("intClass After: " + params.intervals[i].getClass()) + '\n'
				// logFile << "++" + String.valueOf(params.intervals[i]) + '\n'
			}
			bucketor = new BucketingSystem(params.intervals)
			queryResult.getData().each { point ->
				// logFile << getFieldValue(point, params.lonField) + ' ' + getFieldValue(point, params.latField) + '\n'
				// def pointAgg = getFieldValue(point, params.aggregationField)
				bucketor.insert(getFieldValue(point, params.lonField), getFieldValue(point, params.latField))
			}
			aggFieldValue = getFieldValue(queryResult.getData()[0], params.aggregationField)
		}

		//***********************************************************************************************//

		// logFile << "\n**" + String.valueOf("intrvlLength: " + params.i) + '\n'
		def zoomLevel = -1
		for (int i = 0; i < params.intervals.size(); i++) {
			// logFile << "** " + String.valueOf(params.intervals[i]) + '\n'
			if (params.i == params.intervals[i]) {
				zoomLevel = i
			}
		}
		// logFile << String.valueOf(zoomLevel) + '\n'

		Bucket[] buckets = bucketor.getBucketsAtLevel(zoomLevel + 1)

		// logFile << String.valueOf("bucketLength: " + buckets.length) + '\n'
		// logFile << String.valueOf("query Length: " + mapTree.getBucketsAtLevel(zoomLevel+1).length) + '\n'
		// logFile << String.valueOf("first bucket: " + buckets[0]) + '\n'
		// logFile << String.valueOf(buckets) + '\n'

		List<Map<String, Object>> newData = []

		for(int i = 0; i < buckets.length; i++) {
			// logFile << String.valueOf(buckets[i]) + '\n'
			if (buckets[i] == null) {
				break //buckets is big enough to hold every node at the given zoom level, but skips empty nodes;
					  // All non-null items will be in order at the front, and the remainder of the items will be null.
			}

			Map<String, Object> b = [
				data : []
			]
			b.data << [count: buckets[i].count(),
						(params.aggregationField): aggFieldValue,
						centroid: [buckets[i].xCen(), buckets[i].yCen()]]
			// logFile << String.valueOf(b.data) + " " + String.valueOf(b.centroid) + '\n'
			newData << b
			// if (centroid != null || numInBox != 0) {
			// 	logFile << b.left + " " + b.right + " " + b.bottom + " " + b.top +  "    " + b.centroid +  "    " + numInBox + "\n"
			// }
		}

		//The first few lines of the updateData function handle the ouput of this
		//Changed so as to not return the entirety of the input data in addition to the buckets, for no good reason.
		//It now returns the buckets and the size of the original data
		List toReturn = [[data: newData], [data: newData]]
		// 
		// List toReturn = [[data: queryResult.getData()], [data: newData]]
		return new TabularQueryResult(toReturn)

		//These were used for all three variations below
		// int numHorizTiles = Math.round(params.numTilesHorizontal)
		// int numVertTiles = Math.round(params.numTilesVertical)
		// double boxWidth = Math.abs(params.maxLon - params.minLon) / numHorizTiles
		// double boxHeight = Math.abs(params.maxLat - params.minLat) / numVertTiles
		// Map<String, Object>[][] buckets = makeBuckets(params.minLat, params.maxLat, params.minLon, params.maxLon, numHorizTiles, numVertTiles)

		//***********************************************************************************************//

		// queryResult.getData().each { point ->
		// 	def pointAgg = getFieldValue(point, params.aggregationField)
		// 	Set boxes = determineBoxes(getFieldValue(point, params.latField), getFieldValue(point, params.lonField), 
		// 								params.maxLat, params.minLon, boxWidth, boxHeight)
		// 	boxes.each { box ->
		// 		if(box.horizBox < 0 || box.horizBox >= numHorizTiles || box.vertBox < 0 || box.vertBox >= numVertTiles) {
		// 			return
		// 		}
		// 		def addTo = buckets[box.horizBox][box.vertBox].data.find { getFieldValue(it, params.aggregationField) == pointAgg }
		// 		if(addTo) {
		// 			addTo.count += 1
		// 		}
		// 		else {
		// 			buckets[box.horizBox][box.vertBox].data << [count: 1, (params.aggregationField): pointAgg]
		// 		}
		// 	}
		// }

		//***********************************************************************************************//

		// queryResult.getData().each { point ->
		// 	def pointAgg = getFieldValue(point, params.aggregationField)
		// 	Set boxes = determineBoxes(getFieldValue(point, params.latField), getFieldValue(point, params.lonField), 
		// 								params.maxLat, params.minLon, boxWidth, boxHeight)
		// 	boxes.each { box ->
		// 		if(box.horizBox < 0 || box.horizBox >= numHorizTiles || box.vertBox < 0 || box.vertBox >= numVertTiles) {
		// 			return
		// 		}
		// 		Map<String, Object> b = buckets[box.horizBox][box.vertBox];
		// 		def addTo = b.data.find { getFieldValue(it, params.aggregationField) == pointAgg }
		// 		if(addTo) {
		// 			addTo.count += 0;//quadTree.searchRegion(b.left, b.right, b.top, b.bottom)
		// 		}
		// 		else {
		// 			b.data << [count: quadTree.searchRegion(b.left, b.right, b.top, b.bottom), (params.aggregationField): pointAgg]
		// 		}
		// 		Object centroid = quadTree.getCentroidOfRegion(b.left, b.right, b.top, b.bottom)
		// 		if (centroid == null) {
		// 			// b.centroid = [(b.right-b.left)/2,
		// 			// 			  (b.bottom-b.top)/2]
		// 			b.centroid = [0,0]
		// 		}
		// 		else {
		// 			b.centroid = [centroid.x(), centroid.y()]
		// 		}
		// 	}
		// }


		//***********************************************************************************************//

		// def pointAgg = getFieldValue(queryResult.getData().first(), params.aggregationField)
		// def logFile = new File('dxcvLog.txt')
		// logFile << quadTree.getTreeString()

		// for(int x = 0; x < numHorizTiles; x++) {
		// 	for(int y = 0; y < numVertTiles; y++) {
		// 		Map<String, Object> b = buckets[x][y]
		// 		long numInBox = quadTree.searchRegion(b.left, b.right, b.bottom, b.top)
		// 		// if (numInBox == 0) {
		// 		// 	continue
		// 		// }

		// 		b.data << [count: numInBox, (params.aggregationField): ""]
		// 		def centroid = quadTree.getCentroidOfRegion(b.left, b.right, b.bottom, b.top)
				
		// 		if (centroid == null) {
		// 			b.centroid = [0,0]
		// 		}
		// 		else {
		// 			b.centroid = [centroid.x(), centroid.y()]
		// 		}
		// 		// if (centroid != null || numInBox != 0) {
		// 		// 	logFile << b.left + " " + b.right + " " + b.bottom + " " + b.top +  "    " + b.centroid +  "    " + numInBox + "\n"
		// 		// }
		// 	}
		// }


		//***********************************************************************************************//

		// List<Map<String, Object>> newData = []
		// for(int x = 0; x < numHorizTiles; x++) {
		// 	for(int y = 0; y < numVertTiles; y++) {
		// 		if(buckets[x][y].data) {
		// 			newData << buckets[x][y] // THIS IS ONLY A STOPGAP MEASURE. MAKING FEWER BOXES WOULD BE IDEAL. TODO
		// 		}
		// 	}
		// }
		// List toReturn = [[data: queryResult.getData()], [data: newData]]
		// return new TabularQueryResult(toReturn)
	}

	@Override
	String getName() {
		return BucketingTransformer.name
	}

	private Map<String, Object>[][] makeBuckets(double minLat, double maxLat, double minLon, 
												double maxLon, double numHorizTiles, double numVertTiles) {
		Map<String, Object>[][] buckets = new Map<String, Object>[numHorizTiles][numVertTiles]
		for(int x = 0; x < numHorizTiles; x++) {
			for(int y = 0; y < numVertTiles; y++) {
				double l = minLon + (x / numHorizTiles) * Math.abs(maxLon - minLon)
				double r = minLon + ((x + 1) / numHorizTiles) * Math.abs(maxLon - minLon)
				double t = maxLat - (y / numVertTiles) * Math.abs(maxLat - minLat)
				double b = maxLat - ((y + 1) / numVertTiles) * Math.abs(maxLat - minLat)
				buckets[x][y] = [
					left: l,
					right: r,
					top: t,
					bottom: b,
					centroid: [ 0,
								0],
					// centroidX: (r - l) / 2,
					// centroidY: (t - b) / 2,
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