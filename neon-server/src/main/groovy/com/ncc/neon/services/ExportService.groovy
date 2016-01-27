/*
 * Copyright 2015 Next Century Corporation
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

import com.ncc.neon.query.export.ExportBundle
import com.ncc.neon.query.export.ExportRequest
import com.ncc.neon.query.export.ExportQueryRequest
import com.ncc.neon.query.export.ExportArrayCountRequest
import com.ncc.neon.query.export.ExportField
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.result.QueryResult
import org.apache.commons.io.output.CloseShieldOutputStream
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.nio.charset.Charset
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.StreamingOutput
import javax.ws.rs.core.Response

import groovy.json.JsonOutput

/**
 * Service for exporting the data returned by a given query into a file.
 */

@Component
@Path("/exportservice")
class ExportService {

    // Enums are type-safe and so don't work when communicating with javascript, so these are used to define values for what type of file to export.
    // These values match up with the values given for the same-named file types in connection.js
    private static final int CSV = 0
    private static final int XLSX = 1

    // If no fields are given for a query, this is the number of records in the result to search through for fields.
    private static final int NUM_SEARCH_RECORDS = 1000

    // Map for storing uniqueID -> data pairs.
    private static final Map ID_TO_DATA_PAIRS = new ConcurrentHashMap<String, ExportData>()

    @Autowired
    private QueryService queryService

    /**
     * Gets data required for an export request and stores it associated with a unique ID that is returned to the client.
     * This method exists as a way to work around the inability of JQuery's ajax call to accept binary response data. The ID returned to the client
     * serves as part of a URL string that points to the REST endpoint of executeExport. Clientside, the page is immediately redirected to that
     * endpoint, and the browser opens a download window when it realizes it's recieving data it can't open.
     * @param host The host on which the database is running.
     * @param databaseType The type of database.
     * @param bundle The ExportBundle of query requests and associated information to work on.
     * @return The unique identifying string that the data given is mapped to.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("export/{host}/{databaseType}")
    public String makeExportData(@PathParam("host") String host,
                                @PathParam("databaseType") String databaseType,
                                ExportBundle bundle) {
        // The ".zip" here is added purely for cosmetic purposes, as this unique ID string will be the initial name of the file when the download dialog opens.
        String uniqueID = "${UUID.randomUUID()}.zip"
        ExportData data = new ExportData()
        data.host = host
        data.databaseType = databaseType
        data.bundle = bundle
        ID_TO_DATA_PAIRS.put(uniqueID, data)
        Map<String, String> toReturn = [data:uniqueID]
        return JsonOutput.toJson(toReturn)
    }

    /**
     * Generates and streams to the client a zip file containing the results of their requests, in whatever format they requested.
     * @param identifier The unique identifier given to the client by makeExportData, telling where in the map to pull the necessary data from.
     * @return An HTTP response, whose body is a StreamingOutput whose write method streams the created files as they are made. If the requested
     * unique ID is invalid, returns a 400 bad request response.
     */
	@GET
    @Produces("application/zip")
	@Path("generateZip/{uniqueID}")
	public Response executeExport(@PathParam("uniqueID") String identifier) {
        if(ID_TO_DATA_PAIRS[identifier] == null) {
            return Response.status(400).build()
        }
        ExportData data = ID_TO_DATA_PAIRS.remove(identifier)
        return Response.ok(createStreamingOutput(data)).build()
	}

    /**
     * Creates the StreamingOutput to be returned to the client as the content of a successful HTTP request.
     * @param data The data on which the StreamingOutput returned by this method should work.
     * @return the StreamingOutput to be returned to the client.
     */
    private StreamingOutput createStreamingOutput(ExportData data) {
        return new StreamingOutput() {
            public void write(OutputStream output) throws java.io.IOException, WebApplicationException {
                ZipOutputStream zipOut = new ZipOutputStream(output, Charset.forName("UTF-8"))
                try {
                    switch(data.bundle.fileType) {
                        case XLSX:
                            executeExcelExport(data.host, data.databaseType, data.bundle, zipOut)
                            break
                        case CSV:
                        default:
                            executeCSVExport(data.host, data.databaseType, data.bundle, zipOut)
                            break
                    }
                    zipOut.finish()
                    zipOut.close()
                }
                catch (java.io.IOException e) {
                    throw new WebApplicationException(e)
                }
            }
        }
    }

/*
 * ===============================================================================================================================
 * CSV methods.
 * ===============================================================================================================================
 */

    /**
     * Writes a series of .csv files to the provided ZipOutputStream, each one containing the results of one query in the provided ExportBundle.
     * @param host The host on which the database is running.
     * @param databaseType The type of database.
     * @param bundle The ExportBundle of query requests and associated information on which to work.
     * @param output The stream to which to write the output of the export.
     */
	private void executeCSVExport(String host, String databaseType, ExportBundle bundle, ZipOutputStream output) {
		bundle.data.each { request ->
			List<Map<String, Object>> result = getResult(host, databaseType, request)
			addCSV(result, request.fields, request.name, output)
		}
	}

    /**
     * Writes the given fields from the given list of results into a file of the given name on the given output stream.
     * @param result The list of query results from which to pull fields.
     * @param fieldLst The list of fields to pull from the results.
     * @param name The name of the zip file entry into which to place the pulled fields.
     * @param output The stream to which to write the export data.
     */
	private void addCSV(List<Map<String, Object>> result, List<ExportField> fieldList, String name, ZipOutputStream output) {
		output.putNextEntry(new ZipEntry("${name}.csv"))
		List<ExportField> fields = fieldList
        if(fields.size() == 0) {
            fields = getFields(NUM_SEARCH_RECORDS, result)
        }
        List<String> queryNames = []
        // Write out field names in the top row.
        fields.each { field ->
            queryNames.add(field.query)
            output <<"\"${field.pretty.replaceAll("\"", "\"\"")}\","
        }
        output << "\n"
        // Write out field values for each record.
        result.each { record ->
            queryNames.each { field ->
                String s = null
                def fieldValue = record[field] ? record[field] : getFieldInRecord(field, record)
                if(fieldValue instanceof String) {
                    s = fieldValue.replaceAll("\"", "\"\"")
                }
                output << "\"${s?:fieldValue}\","
            }
            output << "\n"
        }
        output.closeEntry()
	}

/*
 * ===============================================================================================================================
 * Excel methods.
 * ===============================================================================================================================
 */

    /**
     * Executes an Excel export request by generating a spreadsheet with one or multiple sheets and writing it to a stream.
     * @param host The host on which the database is running.
     * @param databaseType The type of database.
     * @param bundle The ExportBundle of query requests and associated information on which to work.
     * @param output The stream to which to write the spreadsheet.
     */
    private void executeExcelExport(String host, String databaseType, ExportBundle bundle, ZipOutputStream output) {
        output.putNextEntry(new ZipEntry("${bundle.name}.xlsx"))
        SXSSFWorkbook workbook = new SXSSFWorkbook(500)
        workbook.setCompressTempFiles(true)
        bundle.data.each { request ->
            List<Map<String, Object>> result = getResult(host, databaseType, request)
            generateSheet(workbook, result, request.fields, request.name)
        }
        // Wrapping output stream in a CloseShieldOutputStream because write implicitly closes the stream it's called on.
        // Check http://stackoverflow.com/questions/22321790/xssfworkbook-write-method-closing-output-steam-implicitly for more details.
        workbook.write(new CloseShieldOutputStream(output))
        output.closeEntry()
        workbook.dispose()
    }

    /**
     * Creates a new sheet in the given Excel workbook and writes the given fields or the records in result to it.
     * @param workbook The workbook in which to create the new sheet.
     * @param result The list of results from which to pull field data.
     * @param fields A list of the fields to pull from result and write to the sheet.
     * @param sheetName The name of the sheet to be created.
     */
    private void generateSheet(SXSSFWorkbook workbook, List<Map<String, Object>> result, List<ExportField> fieldList, String sheetName) {
        List<ExportField> fields = fieldList
        if(fields.size() == 0) {
            fields = getFields(NUM_SEARCH_RECORDS, result)
        }
        Sheet sheet = workbook.createSheet()
        workbook.setSheetName(workbook.getNumberOfSheets() - 1, sheetName)
        Row row = sheet.createRow(0)

        int rowNum = 1
        int cellNum = 0
        List<String> queryNames = []
        fields.each { field ->
            queryNames.add(field.query)
            Cell cell = row.createCell(cellNum)
            cell.setCellValue(field.pretty)
            cellNum++
        }
        result.each { record ->
            row = sheet.createRow(rowNum)
            cellNum = 0
            queryNames.each { field ->
                Cell cell = row.createCell(cellNum)
                def fieldValue = record[field] ? record[field] : getFieldInRecord(field, record)
                cell.setCellValue("${fieldValue}")
                cellNum++
            }
            rowNum++
        }
    }

 /*
 * ===============================================================================================================================
 * Helper methods.
 * ===============================================================================================================================
 */

	/**
     * Helper method that looks through the first toLookThrough maps in a list of maps (e.g. the data field of a QueryResult) and returns
     * a list of ExportFields, one for each key found in those maps.
     * @param toLookThrough The maximum number of maps to look through for fields.
     * @param data The list of maps to look through for fields.
     * @return A list of ExportFields.
     */
    private List<ExportField> getFields(int toLookThrough, List<Map<String, Object>> data) {
        int numResults = toLookThrough
        if(data.size() < toLookThrough) {
            numResults = data.size()
        }
        Set fields = [] as Set
        (0..<numResults).each { count ->
            data.get(count).keySet().each { field ->
                fields.add(field)
            }
        }
        List<ExportField> toReturn = []
        fields.each { field ->
            ExportField f = new ExportField()
            f.query = field
            f.pretty = field
            toReturn.add(f)
        }
        return toReturn
    }

	/**
     * Executes the given ExportRequest and returns the list of results.
     * @param host The hostname of the server containing the database.
     * @param databaseType The type of database on which to execute the request.
     * @param request The ExportRequest for which to execute and return results.
     * @return The list of results returned by executing the request.
     */
    private List<Map<String, Object>> getResult(String host, String databaseType, ExportRequest request) {
        List<Map<String, Object>> toReturn = []
        if(request instanceof ExportQueryRequest) {
            QueryResult result = queryService.executeQuery(host, databaseType, request.ignoreFilters, request.selectionOnly, request.ignoredFilterIds, request.query)
            toReturn = result.data
        }
        else if(request instanceof ExportArrayCountRequest) {
            List<ArrayCountPair> results = queryService.getArrayCounts(host, databaseType, request.database, request.table, request.field, request.limit, request?.whereClause)
            results.each { result ->
                Map obj = [:]
                obj.put("key", result.key)
                obj.put("count", result.count)
                toReturn.push(obj)
            }
        }
        return toReturn
    }

    /**
     * Retrieves the field value from the given data. Any field within a field of the root should be specified using
     * a dot (e.g. Retrieving value from subField1 in {field1: {subField1: value}} will be given as "field1.subField1")
     * @param fieldName The field name of the value to retrieve
     * @param result The data to retrieve the value from
     * @return The value of fieldName
     */
    private Object getFieldInRecord(String fieldName, Map result) {
        def fieldNameArray = fieldName.split(/\./)
        def fieldObj = result
        fieldNameArray.each { field ->
            if(fieldObj) {
                fieldObj = fieldObj instanceof Map ? fieldObj.get(field) : null
            }
        }
        return fieldObj
    }

    /**
     * Internal class for convenience. Wraps an ExportBundle, host String, and type String so they can all be mapped under a single value.
     */
    protected class ExportData {
        String host
        String databaseType
        ExportBundle bundle
    }
}
