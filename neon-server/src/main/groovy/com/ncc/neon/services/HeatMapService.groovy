package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.elasticsearch.ElasticSearchHeatmapExecutor
import com.ncc.neon.query.mongo.MongoHeatmapExecutor
import com.ncc.neon.query.result.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Created by jwilliams on 1/27/16.
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
        //modify query with bounding box where clause

        //create executor
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

    //TODO tile calls
}
