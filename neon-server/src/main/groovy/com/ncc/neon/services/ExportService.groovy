/*
 * Copyright 2013 Next Century Corporation
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


import com.ncc.neon.query.*
import com.ncc.neon.query.result.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path("/exportservice")
class ExportService {

    @Autowired
    QueryService queryService

    /**
     * I'll need a better description for this later.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("csv/{host}/{databaseType}")
    /*public String sayHello(@PathParam("host") String host,
                           @PathParam("databaseType") String databaseType,
                           @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                           @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                           @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                           @QueryParam("visualization") String visualization,
                           Query query) {
        System.out.println("Made it this far.");
        QueryResult result = queryService.executeQuery(host, databaseType, ignoreFilters, selectionOnly, ignoredFilterIds, query);
        
        return "{\"title\": \"Hello there, ${visualization}.\"}";
    }*/
    public String sayHello(@PathParam("host") String host,
                           @PathParam("databaseType") String databaseType) {
        return "{\"data\": \"/neon/examples/earthquakes2.csv\"}";
    }
}
