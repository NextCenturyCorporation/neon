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
    public String executeExport(@PathParam("host") String host,
                           @PathParam("databaseType") String databaseType,
                           @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                           @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                           @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                           @QueryParam("visualization") String visualization,
                           Query query) {
        QueryResult result = queryService.executeQuery(host, databaseType, ignoreFilters, selectionOnly, ignoredFilterIds, query);
        File directory = new File("export");
        if(!directory.exists()) {
            directory.mkdir();
        }
        File file = new File("${directory.absolutePath}/" + "test.csv");
        if(!file.exists() && !file.createNewFile()) {
            // Temp code. Needs to fail more gracefully than this. Perhaps just 
            // return an empty string and let the error callback handle it clientside?
            return "{\"data\": \"fileDoesNotExistAndFailedToCreateNewFile\"}";
        }
        if(visualization == "queryResultsTable") {
            generateQueryResultsTable(result, file);
        }
        else if(visualization == "timelineSelector") {
            generateTimelineSelector(result, file);
        }
        else if(visualization == "map") {
            generateMap(result, file);
        }
        else if(visualization == "linechart") {
            generateLineChart(result, file);
        }
        else if(visualization == "barchart") {
            generateBarChart(result, file);
        }
        return "{\"data\": \"/neon/export/${file.name}\"}";
    }

    /**
     * Will also need a better description for this.
     */
    private void generateQueryResultsTable(QueryResult result, File file) {
        List<Map<String, Object>> data = result.data;
        file.write "";
        data[0].keySet().each {
            file << "$it\t"
        };
        file << "\n";
        data.each { record ->
            record.values().each { field ->
                file << "$field\t";
            }; 
            file << "\n";
        };
    }

    /**
     * Guess what? More descriptions needed.
     */
    private void generateTimelineSelector(QueryResult result, File file) {
        List<Map<String, Object>> data = result.data;
        boolean month = false;
        boolean day = false;
        boolean hour = false;
        file.write "Date\tCount\tYear";
        if((data[0])["month"] != null) {
            file << "\tMonth";
            month = true;
        }
        if((data[0])["day"] != null) {
            file << "\tDay";
            day = true;
        }
        if((data[0])["hour"] != null) {
            file << "\tHour";
            hour = true;
        }
        file << "\n";
        data.each {record ->
            file << "${record["date"]}\t${record["count"]}\t${record["year"]}" + 
                (month?"\t${record["month"]}":"") + 
                (day?"\t${record["day"]}":"") + 
                (hour?"\t${record["hour"]}":"") + 
                "\n";
        };
    }

    /**
     * Descriptions will be here eventually, I promise.
     * This is basically the same as generateQueryResults. In the end, 
     * the big difference will be in the names they put on their sheets.
     */
    private void generateMap(QueryResult result, File file) {
        List<Map<String, Object>> data = result.data;
        file.write "";
        data[0].keySet().each {
            file << "$it\t"
        };
        file << "\n";
        data.each { record ->
            record.values().each { field ->
                file << "$field\t";
            }; 
            file << "\n";
        };
    }

    /**
     * Descriptions will be here eventually, I promise.
     */
    private void generateLineChart(QueryResult result, File file) {
        List<Map<String, Object>> data = result.data;
    }

    /**
     * Descriptions will be here eventually, I promise.
     */
    private void generateBarChart(QueryResult result, File file) {
        List<Map<String, Object>> data = result.data;
    }
}
