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


