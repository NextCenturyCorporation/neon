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

	BucketingSystem bucketor;
	// def logFile;
	def querySize = 0;
	def aggFieldValue;
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
		// if (logFile == null) {
		// 	logFile = new File('dxcvLog.txt')
		// }
		// logFile << "\$" + String.valueOf(params.loadNew) + " " + bucketor + " " + queryResult + " " + '\n'
		// logFile << "\t" + params.latField + " " + params.lonField + " " + params.aggregationField + "\n"
		
		// note: despite the check below, if loadNew is false, then bucketor should not be null
		if (params.loadNew || bucketor == null) {
			// logFile << "\t.." + (queryResult != null && queryResult.getData() != null) + "\n"
			if (queryResult != null && queryResult.getData() != null) {
				// logFile << "\t\t1 " + "\n"
				int i = 0
				for (; i < params.intervals.size(); i++) {
					params.intervals[i] = (double)(params.intervals[i])
				}
				// logFile << "\t\t2 " + i + "\n"
				int sum = 0
				bucketor = new BucketingSystem(params.intervals)
				// logFile << "\t\t3 " + "\n"
				def tempPoint = []
				queryResult.getData().each { point ->
					// bucketor.insert(getFieldValue(point, params.lonField), getFieldValue(point, params.latField))
					// logFile << "\t\t\t4a " + sum + " " + 0 + " " + 0 + "\n"
					// def x0 = getFieldValue(point, params.lonField)
					// logFile << "\t\t\t4ba " + x0 + " " + x0.getClass() + "\n"
					double x = getFieldValue(point, params.lonField).toDouble()
					// logFile << "\t\t\t4ba " + x + " " + x.getClass() + "\n"
					double y = getFieldValue(point, params.latField).toDouble()
					// logFile << "\t\t\t4bb " + y + " " + y.getClass() + "\n"
					bucketor.insert(x, y)
					// logFile << "\t\t\t4c \n"
					sum += 1
					tempPoint = point
				}
				// logFile << "\t\t5 " + params.intervals.size() + " " + i + " " + x + " " + y + " " + sum + "\n"
				aggFieldValue = getFieldValue(tempPoint[0], params.aggregationField)
			}
			else {
				List toReturn = [[data: []], [data: []]]
				return new TabularQueryResult(toReturn)
			}
		}


		//***********************************************************************************************//

		def zoomLevel = -1
		for (int i = 0; i < params.intervals.size(); i++) {
			if (params.i == params.intervals[i]) {
				zoomLevel = i
			}
		}

		Bucket[] buckets = bucketor.getBucketsAtLevel(zoomLevel + 1)

		List<Map<String, Object>> newData = []
		int i = 0
		for(; i < buckets.length; i++) {
			if (buckets[i] == null) {
				break //buckets is big enough to hold every node at the given zoom level, but skips empty nodes;
					  // All non-null items will be in order at the front, and the remainder of the items will be null.
			}

			Map<String, Object> b = [
				data : []
			]

			double xCen = buckets[i].xCen()
			double yCen = buckets[i].yCen()
			//centers each point within its bucket; used for development.
			// double xCen = (buckets[i].l() + buckets[i].r())/2
			// double yCen = (buckets[i].t() + buckets[i].b())/2

			b.data << [count: buckets[i].count(),
						(params.aggregationField): aggFieldValue,
						centroid: [xCen, yCen]]
			newData << b
		}
		// logFile << "\$" + buckets.length + " " + i + "\n"

		//The first few lines of the updateData function handle the ouput of this
		//Changed so as to not return the entirety of the input data in addition to the buckets, for no good reason.
		//It now returns the buckets and the size of the original data
		List toReturn = [[data: []], [data: newData]]
		// 
		// List toReturn = [[data: queryResult.getData()], [data: newData]]
		return new TabularQueryResult(toReturn)
	}

	@Override
	String getName() {
		return BucketingTransformer.name
	}


	// private Map<String, Object>[][] makeBuckets(double minLat, double maxLat, double minLon, 
	// 											double maxLon, double numHorizTiles, double numVertTiles) {
	// 	Map<String, Object>[][] buckets = new Map<String, Object>[numHorizTiles][numVertTiles]
	// 	for(int x = 0; x < numHorizTiles; x++) {
	// 		for(int y = 0; y < numVertTiles; y++) {
	// 			double l = minLon + (x / numHorizTiles) * Math.abs(maxLon - minLon)
	// 			double r = minLon + ((x + 1) / numHorizTiles) * Math.abs(maxLon - minLon)
	// 			double t = maxLat - (y / numVertTiles) * Math.abs(maxLat - minLat)
	// 			double b = maxLat - ((y + 1) / numVertTiles) * Math.abs(maxLat - minLat)
	// 			buckets[x][y] = [
	// 				left: l,
	// 				right: r,
	// 				top: t,
	// 				bottom: b,
	// 				centroid: [ 0,
	// 							0],
	// 				// centroidX: (r - l) / 2,
	// 				// centroidY: (t - b) / 2,
	// 				data: []

	// 			]
	// 		}
	// 	}
	// 	return buckets
	// }

	private def getFieldValue(def point, String fieldName) {
		// def logFile1 = new File('dxcvLog1.txt')
		// logFile1 << "getFieldValue " + point[fieldName] + " " + fieldName + "\n"
		// logFile1 << "getField----- " + point + "\n"
		if(point[fieldName] != null) {
			return point[fieldName]
		}
		List pieces = fieldName.split('\\.')
		def currentObject = point
		// int i = 0
		// logFile1 << (pieces)
		// logFile1 << "\n"
		while(pieces.size() > 0 && currentObject != null) {
			currentObject = currentObject[pieces.remove(0)]
			// logFile1 << pieces.size() + " " + i + " " + currentObject + "\n"
			// i++
			// if (i > 10) {
			// 	break
			// }
		}
		return currentObject
	}

	// private Set determineBoxes(def latValue, def lonValue, double maxLat, double minLon, double boxWidth, double boxHeight) {
	// 	if(latValue instanceof Number || latValue instanceof String) {
	// 		return [
	// 			[
	// 				horizBox: (((lonValue as double) - minLon) / boxWidth) as int,
	// 				vertBox: ((maxLat - (latValue as double)) / boxHeight) as int
	// 			]
	// 		]
	// 	}
	// 	// If latValue and lonValue aren't numbers or strings, assume they're lists of some description.
	// 	if(!latValue || !lonValue) {
	// 		return []
	// 	}
	// 	// Zip latValue and lonValue into something of the form [[lat1, lon1]. [lat2, lon2], etc], and then
	// 	// transform each [latX, lonX] pair into a map of the form [horizBox: ___, vertBox: ___]
	// 	List latLonPairs = GroovyCollections.transpose([latValue, lonValue])
	// 	latLonPairs = latLonPairs.collect {
	// 		[
	// 			horizBox: (((it[1] as double) - minLon) / boxWidth) as int,
	// 			vertBox: ((maxLat - (it[0] as double)) / boxHeight) as int
	// 		]
	// 	}
	// 	return latLonPairs as Set // Convert to Set to remove duplicate boxes
	// }
}
