/*
 * Copyright 2014 Next Century Corporation
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

package com.ncc.neon.services.demo
import com.mongodb.DBObject
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.MongoConnectionClient
import groovy.transform.CompileStatic
import org.jongo.Jongo
import org.jongo.MongoCollection
import org.jongo.ResultHandler
import org.springframework.beans.factory.annotation.Autowired

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import java.awt.*
import java.awt.image.BufferedImage
/**
 * A web service that generates map data from a mongo database
 */
@CompileStatic
@Path("/mongomap")
@org.springframework.stereotype.Component
public class MongoMapService {

    // TODO: This radius should be calculated based on zoom factor so it's not tiny when zoomed in
    private static final int DOT_RADIUS = 5

    @Autowired
    private ConnectionManager connectionManager

    // Note these params are all in CAPS because openlayers puts its params in caps and this keeps them consistent

    @SuppressWarnings("MethodSize") // ignore since Jongo requires us to break the query into some longer parts. that makes the method longer, but it's still straight forward@SuppressWarnings("MethodSize") // ignore since Jongo requires us to break the query into some longer parts (and this is a demo class). that makes the method longer, but it's still straight forward
    @SuppressWarnings("ExplicitCallToAndMethod")  // codenarc falsely reports this when using the "and" method in Jongo
    @GET
    @Path("tile")
    @Produces("image/png")
    public Response getTile(@QueryParam("HOST") String host,
                            @QueryParam("DB") String databaseName,
                            @QueryParam("LAYERS") String collectionName,
                            @QueryParam("BBOX") String bbox, @QueryParam("WIDTH") int width,
                            @QueryParam("HEIGHT") final int height) {

        // TODO: We might need to reproject the bounding box depending on the projection of the base map

        connectionManager.currentRequest = (new ConnectionInfo(dataSource: DataSources.mongo, host: host))

        Jongo jongo = new Jongo(((MongoConnectionClient)connectionManager.connection).mongo.getDB(databaseName))

        // TODO: Assumes one layer in LAYERS - is that ok?
        MongoCollection collection = jongo.getCollection(collectionName)

        String[] bboxParts = bbox.split(",")
        //xmin/ymin/xmax/ymax
        float minLon = Float.parseFloat(bboxParts[0])
        float minLat = Float.parseFloat(bboxParts[1])
        float maxLon = Float.parseFloat(bboxParts[2])
        float maxLat = Float.parseFloat(bboxParts[3])

        float pxPerDegreeLon = (float) (width / (maxLon - minLon))
        float pxPerDegreeLat = (float) (height / (maxLat - minLat))

        // TODO: This isn't well tested yet
        // TODO: Assumes fields named location, latitude and longitude. We should be able to get latitude and longitude from location, and not have to store them separately
        // TODO: Modify the query to add the x/y counts and use that for the point density

        // TODO: Need to apply additional neon filters
        // matches only the bounds in the current tile. there should be an index on a field named "location"
        String matchBounds = '{$match : { location : { $geoWithin : { $box : [[' + minLon + ',' + minLat + '],[' + maxLon + ',' + maxLat + ']] } }}}'

        // computes the x/y pixel locations for each location in the tile
        String computeXY = '{$project: { x: {$multiply:[' + pxPerDegreeLon + ', {$subtract:["$longitude",' + minLon + ']} ]}, y: {$multiply:[' + pxPerDegreeLat + ', {$subtract:["$latitude",' + minLat + ']} ]} }}'

        // takes the floor of the x/y so they can be grouped by pixel. mongo aggregation doesn't offer a floor function, so this takes a floor
        // by doing num-num%1 (or num - -1*(num%1) when negative so the floor rounds toward negative infinity)
        String floorXY = '{$project: { x : { $cond: [{ $gte: ["$x", 0] },{$subtract:["$x",{$mod:["$x",1]}]},{$subtract:["$x",{$multiply:[{$mod:["$x",1]},-1]}]}]},y : { $cond: [{ $gte: ["$y", 0] },{$subtract:["$y",{$mod:["$y",1]}]},{$subtract:["$y",{$multiply:[{$mod:["$y",1]},-1]}]}]}}}'

        String groupByXY = '{"$group": {"_id":{x:"$x",y:"$y"}}}'

        // TODO: Replace this with opengl rendering (see TilingRenderer in the gltiles project) - maybe not for the demo though since it adds extra dependencies that may not be necessary
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        Graphics2D graphics = image.createGraphics()
        graphics.setColor(Color.RED)
        collection.aggregate(matchBounds).and(computeXY).and(floorXY).and(groupByXY).map(new ResultHandler<Void>() {
            @Override
            public Void map(DBObject result) {
                // TODO: Is the DOT_RADIUS being subtracted in the right direction for the height?
                DBObject id = (DBObject) result.get("_id")
                graphics.fillOval(((Number) id.get("x")).intValue() - DOT_RADIUS,
                        height - ((Number) id.get("y")).intValue() - DOT_RADIUS,
                        DOT_RADIUS, DOT_RADIUS)
                return null
            }
        })
        return Response.ok(image).build()
    }


}
