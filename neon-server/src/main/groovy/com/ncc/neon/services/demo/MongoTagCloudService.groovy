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
import com.mongodb.DB
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
/**
 * A web service that generates a frequency map of values from an array
 */
@Path("/mongotagcloud")
@Component
public class MongoTagCloudService {

    @Autowired
    private ConnectionManager connectionManager

    @GET
    @Path("tagcounts")
    @Produces("application/json;charset=UTF-8")
    public Map<String, Integer> getTile(@QueryParam("host") String host,
                                        @QueryParam("db") String databaseName,
                                        @QueryParam("collection") String collectionName,
                                        @QueryParam("arrayfield") String arrayField,
                                        @DefaultValue("50") @QueryParam("limit") int limit
    ) {
        connectionManager.currentRequest = (new ConnectionInfo(dataSource: DataSources.mongo, host: host))
        DB database = connectionManager.connection.mongo.getDB(databaseName)
        return MongoTagCloudBuilder.getTagCounts(database, collectionName, arrayField, limit)
    }
}
