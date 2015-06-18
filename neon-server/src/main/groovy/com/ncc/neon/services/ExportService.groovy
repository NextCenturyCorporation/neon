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
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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

    // Enums are type-safe and so don't work when communicating with javascript, so these are used to dfiene values for what type of file to export.
    // These values match up with the values given for the same-named file types in connection.js
    static final int CSV = 0
    static final int XLSX = 1

    // If no fields are given for a query, this is the number of records in the result to search through for fields.
    static final int NUM_SEARCH_RECORDS = 1000

    // The name of the directory in which to place files for export after they are created.
    static final String dirPath = "export"

    @Autowired
    QueryService queryService

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("export/{host}/{databaseType}")
    public String executeExport(@PathParam("host") String host,
                                @PathParam("databaseType") String databaseType,
                                ExportBundle bundle) {
        String ret
        switch(bundle.fileType) {
            case CSV:
                ret = executeCSVExport(host, databaseType, bundle)
                break
            case XLSX:
                ret = executeExcelExport(host, databaseType, bundle)
                break
        }
        return ret
    }

    /**
     * Executes a CSV export request by generating one or multiple CSV files and 
     * returning a link to it or to a .zip archive of them, if there are multiple.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param data A list of maps, each one containing a query, the filename that query's data should be written to,
     * and a list of fields to write data from.
     * @return A link to the generated .csv file or .zip archive.
     */
    public String executeCSVExport(String host, String databaseType, ExportBundle bundle) {
        List<File> files = []
        bundle.data.each { exportQuery ->
            QueryResult result = queryService.executeQuery(host, databaseType, exportQuery.ignoreFilters, exportQuery.selectionOnly, exportQuery.ignoredFilterIds, exportQuery.query)
            files.add(generateCSV(result, exportQuery.fields, exportQuery.name))
        }
        String exportDir = getExportDir()
        HashMap<String, String> toReturn = new HashMap<String, String>()
        if(files.size() > 1) {
            String result = generateZipFile("${exportDir}${File.separator}${bundle.name}.zip", "${bundle.name}.zip", files);
            toReturn.put("data", result)
        }
        else if(files.size()) {
            toReturn.put("data","/neon/${dirPath}/${files[0].name}")
        }

        return JsonOutput.toJson(toReturn)
    }

    /**
     * Generates a CSV file.
     * @param result the QueryResult containing the records to write data from.
     * @param fields A list of maps that represent the fields to write from the records, both as they will appear in the QueryResult and in prettified forms.
     * e.g. [{"query":"field1", "pretty":"Field 1"}, {"query":"field2", "pretty":"Field 2"}]
     * @param fileName The name of the file to find or create and write data to.
     * @return the File object representing the file written to.
     */
    private File generateCSV(QueryResult result, List<ExportField> fields, String fileName) {
        if(fields.size() == 0) {
            fields = getFields(NUM_SEARCH_RECORDS, result.data)
        }
        File file = createCSVWithName(fileName)
        // Clear file of anything that was in it before.
        file.write ""
        // Write out field names in top row.
        List<String> queryNames = []
        fields.each { field ->
            queryNames.add(field.query)
            file << "${field.pretty}\t"
        }
        file << "\n"
        // Write out field values for each record.
        List<Map<String, Object>> data = result.data
        data.each { record ->
            queryNames.each { field ->
                file << "${record[field]}\t"
            }
            file << "\n"
        }
        return file
    }

    /**
     * Opens or creates a Fileobject for a CSV file with the given name and returns it. If a file of the given name is unable to be found
     * or created, returns an error message.
     * @param name The name - not including file extension - of the CSV file to be created.
     * @return The File object representing the CSV file with the given name, or an error message if one could not be found or created.
     */
    private File createCSVWithName(String name) {
        String exportDir = getExportDir()
        File file = new File("${exportDir}${File.separator}${name}.csv")
        if(!file.exists() && !file.createNewFile()) {
            return Response.status(Response.status.INTERNAL_SERVER_ERROR).entity("Could not find or create a file.").type(MediaType.APPLICATION_JSON).build()
        }
        return file
    }

    /**
     * Helper method that makes sure the export directory on the server exists before returning a string containing it's absolute path on disk.
     * @return The absolute path of the export directory.
     */
    private String getExportDir() {
        File directory = new File("$dirPath")
        if(!directory.exists()) {
            directory.mkdir()
        }
        return directory.absolutePath
    }

    /** 
     * Generates a zip file at the provided location on disk, with the provided name, and containing the files in the provided list.
     * @param filePath The absolute path of the zip file to create - e.g. "/server/location/on/disk/export/test.zip".
     * @param fileName The name of the zip file to create, without the path - e.g. "test.zip".
     * @param files A list of File objects representing the files to be added to the zip file.
     * @return The server URL of the zip file created. Should always be /neon/export/${fileName} unless something is changed in executeCSVExport().
     */
    private String generateZipFile(String filePath, String fileName, List<File> files) {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(filePath))
        files.each { toZip ->
            byte[] bytes = toZip.getBytes()
            if(bytes.length) {
                zip.putNextEntry(new ZipEntry(toZip.name))
                zip.write(bytes, 0, bytes.length)
                zip.closeEntry()
            }
        }
        zip.close()
        return "/neon/export/${fileName}"
    }

    /**
     * Executes an Excel export request by generating a spreadsheet with one or multiple sheets and returning a link to it.
     * @param host The host the database is running on
     * @param databaseType the type of database
     * @param data A list of maps, each one containing a query, the name of the sheet that query's data should be written to,
     * and a list of fields to write data from.
     * @return A link to the generated .xlsx file.
     */
    public String executeExcelExport(String host, String databaseType, ExportBundle bundle) {
        XSSFWorkbook wb = new XSSFWorkbook()
        File file = createExcelWithName(bundle.name)
        bundle.data.each { exportQuery ->
            QueryResult result = queryService.executeQuery(host, databaseType, exportQuery.ignoreFilters, exportQuery.selectionOnly, exportQuery.ignoredFilterIds, exportQuery.query)
            generateSheet(wb, result, exportQuery.fields, exportQuery.name)
        }

        FileOutputStream out = new FileOutputStream(file)
        wb.write(out)
        out.close()

        HashMap<String, String> toReturn = new HashMap<String, String>()
        toReturn.put("data", "/neon/export/${file.name}")
        return JsonOutput.toJson(toReturn)
    }

    /**
     * Creates a new sheet in the given Excel workbook and 
     */
    private void generateSheet(XSSFWorkbook wb, QueryResult result, List<ExportField> fields, String sheetName) {
        if(fields.size() == 0) {
            fields = getFields(NUM_SEARCH_RECORDS, result.data)
        }
        Sheet sheet = wb.createSheet()
        wb.setSheetName(wb.getNumberOfSheets() - 1, sheetName)
        Row headers = sheet.createRow(0)
        Cell c = null

        int rowNum = 1
        int cellNum = 0
        List<String> queryNames = []
        fields.each { field ->
            queryNames.add(field.query)
            c = headers.createCell(cellNum)
            c.setCellValue(field.pretty)
            cellNum++
        }

        Row r = null
        result.data.each { record ->
            r = sheet.createRow(rowNum)
            cellNum = 0
            queryNames.each { field ->
                c = r.createCell(cellNum)
                c.setCellValue("${record[field]}")
                cellNum++
            }
            rowNum++
        }
    }

    /**
     * Opens or creates a Fileobject for an Excel file with the given name and returns it. If a file of the given name is unable to be found
     * or created, returns an error message.
     * @param name The name - not including file extension - of the Excel file to be created.
     * @return The File object representing the Excel file with the given name, or an error message if one could not be found or created.
     */
    private File createExcelWithName(String name) {
        File directory = new File("export")
        if(!directory.exists()) {
            directory.mkdir()
        }
        File file = new File("${directory.absolutePath}${File.separator}${name}.xlsx")
        if(!file.exists() && !file.createNewFile()) {
            return Response.status(Response.status.INTERNAL_SERVER_ERROR).entity("Could not find or create a file.").type(MediaType.APPLICATION_JSON).build()
        }
        return file
    }

    /**
     * Helper method that looks through the first toLookThrough maps in a list of maps (e.g. the data field of a QueryResult) and returns 
     * a list of ExportFields, one for each key found in those maps.
     * @param toLookThrough The maximum number of maps to look through for fields.
     * @param data The list of maps to look through for fields.
     * @return a list of ExportFields
     */
    private List<ExportField> getFields(int toLookThrough, List<Map<String, Object>> data) {
        int numResults = toLookThrough
        if(data.size() < toLookThrough) {
            numResults = data.size()
        }
        HashSet<String> fields = new HashSet<String>()
        for(int count = 0; count < numResults; count++) {
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
}