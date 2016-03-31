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

package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Service for executing heatmap queries against an arbitrary data store.
 */

@Component
@Path("/heatmapservice")
class HeatMapService {

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query/{host}/{databaseType}")
    QueryResult queryData(@PathParam("host") String host,
                        @PathParam("databaseType") String databaseType,
                        @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                        @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                        @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                        @QueryParam("minLat") float minLat,
                        @QueryParam("maxLat") float maxLat,
                        @QueryParam("minLon") float minLon,
                        @QueryParam("maxLon") float maxLon,
                        @QueryParam("latField") String latField,
                        @QueryParam("lonField") String lonField,
                        @QueryParam("locationField") String locationField,
                        @DefaultValue("8") @QueryParam("gridCount") int gridCount,

                        Query query) {

        def executor = queryExecutorFactory.getExecutor(new ConnectionInfo(host: host, dataSource: databaseType as DataSources), true)

        HeatmapBoundsQuery boundingBox = new HeatmapBoundsQuery([
                minLat: minLat,
                maxLat: maxLat,
                minLon: minLon,
                maxLon: maxLon,
                latField: latField,
                lonField: lonField,
                locationField: locationField,
                gridCount: gridCount
        ])

        executor.execute(query, new QueryOptions(ignoreFilters: ignoreFilters,
            selectionOnly: selectionOnly, ignoredFilterIds: (ignoredFilterIds ?: ([] as Set))), boundingBox)
    }
}
