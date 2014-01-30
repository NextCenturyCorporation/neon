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

import com.ncc.neon.language.QueryParser
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType



/**
 * Service for querying generic data stores using a SQL-like query language
 */

@Component
@Path("/languageservice")
class LanguageService {

    @Autowired
    QueryParser queryParser

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    /**
     * Executes a query using a TQL query.
     * @param text The query string that will be parsed and converted into a query.
     * @return The query result data
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    QueryResult executeQuery(@FormParam("text") String text) {
        Query query = queryParser.parse(text)
        return queryExecutorFactory.getExecutor().execute(query, QueryOptions.FILTERED_DATA)
    }

}


