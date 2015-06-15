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
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.result.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path("/exportservice")
class ExportService {

    @Autowired
    QueryService queryService

    /**
     * Executes a CSV export request by generating one or multiple CSV files and 
     * returning a link to it or to a .zip archive of them, if there are multiple.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param data A list of maps, each one containing a query, the filename that query's data should be written to,
     * and a list of fields to write data from.
     * @return A link to the generated .csv file or .zip archive.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("csv/{host}/{databaseType}")
    public String executeCSVExport(@PathParam("host") String host,
                            @PathParam("databaseType") String databaseType,
                           List<Map<String, Object>> data) {
        List<File> files = [];
        String toReturn;
        data.each { query_fields_object ->
            System.out.println(query_fields_object)
            File file = createCSVFileForQuery(query_fields_object)
            files.add(file)
            generateCSV(result, file, query_fields_object.fields)
        }
        if(files.size() > 1) {
            // Will eventually replace ${test} with user ID or something like that, as well as '/' with '${File.pathSeparator}'.
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream("${directory.absolutePath}/test.zip"))
            files.each { toZip ->
                byte[] bytes = toZip.getBytes()
                if(bytes.length) {
                    zip.putNextEntry(new ZipEntry(toZip.name))
                    zip.write(bytes, 0, bytes.length)
                    zip.closeEntry()
                }
            }
            zip.close()
            // TODO replace 'test.zip' with user ID.zip or something like that.
            toReturn = "{\"data\": \"/neon/export/test.zip\""
        }
        else if(files.size()) {
            toReturn = "{\"data\": \"/neon/export/${files[0].name}\"}"
        }
        return toReturn
    }

    private File createCSVFileForQuery(Map<String, Object> query_fields_object) {
        // Since we're not getting a Query object directly, we need to remove ignoreFilters_,
        // selectionOnly_, and ignoredFilterIds_ manually.
        boolean ignoreFilters = query_fields_object.query.ignoreFilters_
        boolean selectionOnly = query_fields_object.query.selectionOnly_
        Set<String> ignoredFilterIds = query_fields_object.query.ignoredFilterIds_
        query_fields_object.query.remove("ignoreFilters_")
        query_fields_object.query.remove("selectionOnly_")
        query_fields_object.query.remove("ignoredFilterIds_")
        // Query query = processQuery(query_fields_object.query)
        QueryResult result = queryService.executeQuery(host, databaseType, ignoreFilters, selectionOnly, ignoredFilterIds, (Query)query_fields_object.query)
        File directory = new File("export")
        if(!directory.exists()) {
            directory.mkdir()
        }
        // For some reason this breaks when I replace '/' with '${File.pathSeparator}'. I'm not actually certain why at this point.
        File file = new File("${directory.absolutePath}/${query_fields_object.name}.csv")
        if(!file.exists() && !file.createNewFile()) {
            return Response.status(Response.status.INTERNAL_SERVER_ERROR).entity("Could not find or create a file.").type(MediaType.APPLICATION_JSON).build();
        }
        return file;
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

    /**
     * Convert a LinkedHashMap to a Query. This function replaces the automatic processing normally done when a query is passed in without 
     * being wrapped up in another object. This needs to happen after the removal of ignoreFilters_, selectionOnly_, and ignoredFilterIds_,
     * because those values are needed and do not get added back into the Query after conversion.
     */
     private Query processQuery(LinkedHashMap query) {
        LinkedHashMap oldWhereClause = query.filter.whereClause
        WhereClause whereClause
        if(oldWhereClause == null) {
            whereClause = null
        } else if(oldWhereClause.type == "where") {
            whereClause = new SingularWhereClause()
            whereClause.lhs = oldWhereClause.lhs
            whereClause.operator = oldWhereClause.operator
            whereClause.rhs = oldWhereClause.rhs
        } else if(oldWhereClause.type == "withinDistance") {

        } else if(oldWhereClause.type == "geoIntersection") {

        } else if(oldWhereClause.type == "geoWithin") {

        } else if(oldWhereClause.type == "and") {

        } else if(oldWhereClause.type == "or") {

        }
        query.filter.whereClause = []
        List<LinkedHashMap> oldGroupByClauses = query.groupByClauses
        List<? extends GroupByClause> groupByClauses = []
        query.groupByClauses = null

        Query queryObject = (Query)query
        queryObject.filter.whereClause = whereClause
        return queryObject
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