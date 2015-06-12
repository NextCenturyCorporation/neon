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
     * Executes an export request by generating one or multiple CSV files and 
     * returning a link to it or to a .zip archive of them, if there are multiple.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param data A list of maps, each one containing a query, the filename that query's data should be written to,
     * and a list of fields to write data from.
     * @return A link to the generated CSV file or .zip archive.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("csv/{host}/{databaseType}")
    public String executeExport(@PathParam("host") String host,
                            @PathParam("databaseType") String databaseType,
                           List<Map<String, Object>> data) {
        List<String> files = [];
        String toReturn;
        data.each { query_fields_object ->
            boolean ignoreFilters = query_fields_object.query.ignoreFilters_
            boolean selectionOnly = query_fields_object.query.selectionOnly_
            Set<String> ignoredFilterIds = query_fields_object.query.ignoredFilterIds_
            query_fields_object.query.remove("ignoreFilters_")
            query_fields_object.query.remove("selectionOnly_")
            query_fields_object.query.remove("ignoredFilterIds_")
            QueryResult result = queryService.executeQuery(host, databaseType, ignoreFilters, selectionOnly, ignoredFilterIds, (Query)query_fields_object.query)
            File directory = new File("export")
            if(!directory.exists()) {
                directory.mkdir()
            }
            File file = new File("${directory.absolutePath}/${query_fields_object.name}.csv")
            if(!file.exists() && !file.createNewFile()) {
                return Response.status(Response.status.INTERNAL_SERVER_ERROR).entity("Could not find or create a file.").type(MediaType.APPLICATION_JSON).build();
            }
            files.add(file.name)
            generateCSV(result, file, query_fields_object.fields)
        }
        if(files.size() > 1) {
            // TODO Zip up all the files into a .zip
            // Set toReturn equal to the .zip file
        }
        else if(files.size()) {
            toReturn = "{\"data\": \"/neon/export/${files[0]}\"}"
        }
        return toReturn
    }

    /**
     * Generates a CSV file.
     * @param result the QueryResult containing the records to write data from.
     * @param file The file to write data to.
     * @param fields A list of maps that represent the fields to write from the records, both as they will appear in the QueryResult and in prettified forms. 
     * e.g. [{"query":"field1", "pretty":"Field 1"}, {"query":"field2", "pretty":"Field 2"}]
     */
    private void generateCSV(QueryResult result, File file, List<Map<String, String>> fields) {
        file.write ""
        List<String> queryNames = []
        fields.each { field ->
            queryNames.add(field["query"])
            file << "${field["pretty"]}\t"
        }
        file << "\n"
        List<Map<String, Object>> data = result.data
        data.each { record ->
            queryNames.each { field ->
                file << "${record[field]}\t"
            }
            file << "\n"
        }
    }
}

/**
 * {[
 *     {
 *         "query": query object,
 *         "name": name of file w/o extension,
 *         "fields": [{
 *                       "query": name of field in query object,
 *                       "pretty": prettified name of field
 *                   }, {
 *                       etc.
 *                   }
 *               ]
 *     }, {
 *         "query": next query object,
 *          etc.
 *     }
 * ]}
 */