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
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.MongoConnectionClient
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
/**
 * A web service that generates map data from a mongo database
 */
@CompileStatic
@Path("/mongomap")
@Component
public class MongoMapService {

    // TODO: Extract methods - this is really long right now
    // TODO: Assumes one layer in LAYERS - is that ok?

    @Autowired
    private ConnectionManager connectionManager

    @Autowired
    private MongoNeonHelper mongoNeonHelper

    // Note these params are all in CAPS because openlayers puts its params in caps and this keeps them consistent

    @SuppressWarnings("MethodSize")
    @GET
    @Path("tile")
    @Produces("image/png")
    public Response getTile(@QueryParam("HOST") String host,
                            @QueryParam("DB") String databaseName,
                            @QueryParam("LAYERS") String collectionName,
                            @QueryParam("BBOX") String bbox, @QueryParam("WIDTH") int width,
                            @QueryParam("HEIGHT") final int height,
                            @DefaultValue("0.5f") @QueryParam("MINALPHA") final float minAlpha,
                            @DefaultValue("100") @QueryParam("MAXCOUNT") final int maxCount

    ) {


        connectionManager.currentRequest = (new ConnectionInfo(dataSource: DataSources.mongo, host: host))

        String[] bboxParts = bbox.split(",") //bounding box //xmin/ymin/xmax/ymax
        float minLon = Float.parseFloat(bboxParts[0])
        float minLat = Float.parseFloat(bboxParts[1])
        float maxLon = Float.parseFloat(bboxParts[2])
        float maxLat = Float.parseFloat(bboxParts[3])

        float pxPerDegreeLon = (float) (width / (maxLon - minLon)) //how to convert the lat to pixels for the image
        float pxPerDegreeLat = (float) (height / (maxLat - minLat)) // not sure if this is still needed


        int dotRadius
        int minDegrees = (int) Math.min((maxLon - minLon), (maxLat - minLat))
        if (minDegrees >= 22.5) {
            dotRadius = 8
        } else {
            dotRadius = 16
        }

        // TODO: Assumes fields named location, latitude and longitude.
        // TODO: Modify the query to add the x/y counts and use that for the point density

        DBObject box = new BasicDBObject('$box', [[minLon, minLat], [maxLon, maxLat]])
        DBObject geoWithin = new BasicDBObject('$geoWithin', box)
        DBObject matchQuery = new BasicDBObject('location', geoWithin)
        matchQuery = mongoNeonHelper.mergeWithNeonFilters(matchQuery, databaseName, collectionName)
        DBObject match = new BasicDBObject('$match', matchQuery) //find where location within the bounding box



        DBObject lon = new BasicDBObject('$subtract', ['$longitude', minLon])
        DBObject x = new BasicDBObject('$multiply', [pxPerDegreeLon, lon])
        DBObject lat = new BasicDBObject('$subtract', ['$latitude', minLat])
        DBObject y = new BasicDBObject('$multiply', [pxPerDegreeLat, lat])


        DBObject modX = new BasicDBObject('$mod', [x, 1])
        DBObject floorXPos = new BasicDBObject('$subtract', [x, modX])
        DBObject multNegOneModX = new BasicDBObject('$multiply', [-1, floorXPos])
        DBObject floorXNeg = new BasicDBObject('$subtract', [x, multNegOneModX])
        BasicDBList gteZeroXArgs = new BasicDBList()
        gteZeroXArgs.add(x)
        gteZeroXArgs.add(0)
        DBObject gteZeroX = new BasicDBObject('$gte', [x, 0])


        DBObject modY = new BasicDBObject('$mod', [y, 1])
        DBObject floorYPos = new BasicDBObject('$subtract', [y, modY])
        DBObject multNegOneModY = new BasicDBObject('$multiply', [-1, floorYPos])
        DBObject floorYNeg = new BasicDBObject('$subtract', [y, multNegOneModY])
        BasicDBList gteZeroYArgs = new BasicDBList()
        gteZeroYArgs.add(y)
        gteZeroYArgs.add(0)
        DBObject gteZeroY = new BasicDBObject('$gte', [y, 0])

        BasicDBList xConditions = new BasicDBList()
        xConditions.add(gteZeroX)
        xConditions.add(floorXPos)
        xConditions.add(floorXNeg)
        DBObject condX = new BasicDBObject('$cond', xConditions)

        BasicDBList yConditions = new BasicDBList()
        yConditions.add(gteZeroY)
        yConditions.add(floorYPos)
        yConditions.add(floorYNeg)
        DBObject condY = new BasicDBObject('$cond', yConditions)

        DBObject fields = new BasicDBObject()
        fields.put("x", condX)
        fields.put("y", condY)
        DBObject project = new BasicDBObject('$project', fields)

        DBObject idField = new BasicDBObject()
        idField.put("x", '$x')
        idField.put("y", '$y')
        DBObject groupFields = new BasicDBObject("_id", idField)
        groupFields.append("count", new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        Graphics2D graphics = image.createGraphics()

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f))

        // TODO: Replace this with opengl rendering (see TilingRenderer in the gltiles project) - maybe not for the demo though since it adds extra dependencies that may not be necessary
        MongoClient mongo = ((MongoConnectionClient) connectionManager.connection).mongo
        Iterator<DBObject> results = mongo.getDB(databaseName).getCollection(collectionName).aggregate(match, project, group).results().iterator()
        while (results.hasNext()) {
            DBObject row = results.next()
            DBObject id = (DBObject) row.get("_id")
            int count = ((Number) row.get("count")).intValue()
            // assume 0 as a min count
            float pct = (float) (count / (float) maxCount)
            float alpha = pct * (1.0f - minAlpha) + minAlpha
            if ( alpha > 1.0f ) {
                alpha = 1.0f
            }
            else if ( alpha < 0.0f ) {
                alpha = 0.0f
            }
            graphics.setColor(new Color(1f,0f,0f,alpha))
            graphics.fillOval(((Number) id.get("x")).intValue() - dotRadius,
                    height - ((Number) id.get("y")).intValue() - dotRadius,
                    dotRadius, dotRadius)
        }
        return Response.ok(image).build()
    }

}
