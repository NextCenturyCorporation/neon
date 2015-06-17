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
import com.ncc.neon.query.export.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.result.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.core.Response
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

import groovy.json.JsonOutput

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
                           List<ExportQuery> data) {
        List<File> files = []
        data.each { exportQuery ->
            QueryResult result = queryService.executeQuery(host, databaseType, exportQuery.ignoreFilters, exportQuery.selectionOnly, exportQuery.ignoredFilterIds, exportQuery.query)
            File file = createCSVWithName(exportQuery.name)
            files.add(file)
            generateCSV(result, file, exportQuery.fields)
        }
        HashMap<String, String> toReturn = new HashMap<String, String>()
        if(files.size() > 1) {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream("${directory.absolutePath}${file.separator}test.zip"))
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
            toReturn.put("data", "/neon/export/test.zip")
        }
        else if(files.size()) {
            toReturn.put("data","/neon/export/${files[0].name}")
        }
        return JsonOutput.toJson(toReturn)
    }

    /**
     * Opens or creates a Fileobject for a CSV file with the given name and returns it. If a file of the given name is unable to be found
     * or created, returns an error message.
     * @param name The name - not including file extension - of the CSV file to be created.
     * @return The File object representing the CSV file with the given name, or an error message if one could not be found or created.
     */
    private File createCSVWithName(String name) {
        File directory = new File("export")
        if(!directory.exists()) {
            directory.mkdir()
        }
        File file = new File("${directory.absolutePath}${File.separator}${name}.csv")
        if(!file.exists() && !file.createNewFile()) {
            return Response.status(Response.status.INTERNAL_SERVER_ERROR).entity("Could not find or create a file.").type(MediaType.APPLICATION_JSON).build()
        }
        return file
    }

    /**
     * Generates a CSV file.
     * @param result the QueryResult containing the records to write data from.
     * @param file The file to write data to.
     * @param fields A list of maps that represent the fields to write from the records, both as they will appear in the QueryResult and in prettified forms. 
     * e.g. [{"query":"field1", "pretty":"Field 1"}, {"query":"field2", "pretty":"Field 2"}]
     */
    private void generateCSV(QueryResult result, File file, List<ExportField> fields) {
        file.write ""
        List<String> queryNames = []
        fields.each { field ->
            queryNames.add(field.query)
            file << "${field.pretty}\t"
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