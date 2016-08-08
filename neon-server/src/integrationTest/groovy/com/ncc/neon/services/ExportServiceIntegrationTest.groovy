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

import com.ncc.neon.IntegrationTestContext

import org.json.JSONObject

import org.junit.Assume
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith

import org.springframework.test.context.ContextConfiguration

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.springframework.beans.factory.annotation.Autowired

import javax.ws.rs.core.Response

import com.ncc.neon.query.export.ExportBundle
import com.ncc.neon.query.export.ExportField
import com.ncc.neon.query.export.ExportQueryRequest
import com.ncc.neon.query.export.ExportArrayCountRequest

import com.ncc.neon.util.LatLon

import com.ncc.neon.query.Query

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.DistanceUnit
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.ncc.neon.query.filter.Filter

/**
 * Integration test that verifies the neon server properly imports data into mongo
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class ExportServiceIntegrationTest {

    private ExportService exportService

    private static final String HOST = System.getProperty("mongo.host")
    private static final String DATABASE_TYPE = "mongo"
    private static final String DATABASE_NAME = "neonintegrationtest"
    private static final String TABLE_NAME = "records"
    private static final String RESULT_CSV_STRING1 = '"_id","First Name","Last Name","State","Location",\n"5137b623a9f279d831b6fb88","Maggie","Thomas","DC","{\"\"coordinates\"\":[19.55,11.92],\"\"type\"\":\"\"Point\"\"}",\n'
    private static final String RESULT_CSV_STRING2 = '"key","count",\n"tag2","8",\n"tag3","2",\n"tag1","1",\n'
    private static final String RESULT_EXCEL_STRING = 'Key, Count, \ntag2, 8, \ntag3, 2, \ntag1, 1, \n\n_id, city, firstname, hiredate, lastname, location, salary, state, tags, \n5137b623a9f279d831b6fb88, Washington, Maggie, Fri Oct 14 20:00:00 EDT 2011, Thomas, {"coordinates":[19.55,11.92],"type":"Point"}, 175000, DC, ["tag2","tag3"], \n\n'

    private static Query query

    private QueryService queryService

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService
    }

    @Before
    void before() {
        // Establish the connection, or skip the tests if no host was specified
        Assume.assumeTrue(HOST != null && HOST != "")
        exportService = new ExportService()
        exportService.queryService =  this.queryService

        def withinDistance = new WithinDistanceClause(
            locationField: "location",
            center: new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d),
            distance: 35d,
            distanceUnit: DistanceUnit.MILE
        )
        def dcStateClause = new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC')
        def whereClause = new AndWhereClause(whereClauses: [withinDistance, dcStateClause])
        query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: whereClause))
    }

    @Test
    void "make export data test"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest()])
        String response = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(response)
        assert obj.data
        assert exportService.ID_TO_DATA_PAIRS[obj.data]
        assert exportService.ID_TO_DATA_PAIRS[obj.data].host == HOST
        assert exportService.ID_TO_DATA_PAIRS[obj.data].databaseType == DATABASE_TYPE
        assert exportService.ID_TO_DATA_PAIRS[obj.data].bundle == bundle
    }

    @Test
    void "execute export test for csv file"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest(
            query: query,
            name: "test-export-1",
            fields: [
                new ExportField(query: "_id", pretty: "_id"),
                new ExportField(query: "firstname", pretty: "First Name"),
                new ExportField(query: "lastname", pretty: "Last Name"),
                new ExportField(query: "state", pretty: "State"),
                new ExportField(query: "location", pretty: "Location")
            ]
        ), new ExportArrayCountRequest(
            database: DATABASE_NAME,
            table: TABLE_NAME,
            field: "tags",
            limit: 40,
            name: "test-export-2",
            fields: [])
        ])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        Response response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))
        byte[] buffer = new byte[155]
        ZipEntry entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-1.csv"
        zipIn.read(buffer)
        assert new String(buffer, "UTF-8") == RESULT_CSV_STRING1
        zipIn.closeEntry()
        buffer = new byte[51]
        entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-2.csv"
        zipIn.read(buffer)
        assert new String(buffer, "UTF-8") == RESULT_CSV_STRING2
        zipIn.close()
    }

    @Test
    void "execute export test for xlsx file"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 1, data: [new ExportArrayCountRequest(
            database: DATABASE_NAME,
            table: TABLE_NAME,
            field: "tags",
            limit: 40,
            name: "test-export-1",
            fields: [
                new ExportField(query: "key", pretty: "Key"),
                new ExportField(query: "count", pretty: "Count")
            ]
        ), new ExportQueryRequest(
            query: query,
            name: "test-export-2",
            fields: [],
            ignoreFilters: false,
            selectionOnly: false,
            ignoredFilterIds: []
        )])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        Response response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.toByteArray())
        ZipInputStream zipIn = new ZipInputStream(byteIn)
        assert getExcelSheetAsString(zipIn) == RESULT_EXCEL_STRING
        zipIn.close()
        byteIn.close()
    }

    @SuppressWarnings('MethodSize')
    private String getExcelSheetAsString(ZipInputStream zipIn) {
        String excelString = ""
        ZipEntry ze = zipIn.getNextEntry()
        assert ze != null
        assert ze.getName() == "test.xlsx"

        int size
        byte[] buffer = new byte[2048]

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream()
        BufferedOutputStream bufferOut = new BufferedOutputStream(byteOut)

        while((size = zipIn.read(buffer, 0, buffer.length)) != -1) {
            bufferOut.write(buffer, 0, size)
        }

        bufferOut.flush()
        bufferOut.close()

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray())
        Workbook workbook = new XSSFWorkbook(byteIn)

        for(int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i)
            Iterator<Row> iterator = sheet.iterator()
            assert sheet.getSheetName() == "test-export-" + (i + 1)

            while(iterator.hasNext()) {
                Row nextRow = iterator.next()
                Iterator<Cell> cellIterator = nextRow.cellIterator()

                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next()

                    switch(cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            excelString += cell.getStringCellValue() + ", "
                            break
                        case Cell.CELL_TYPE_BOOLEAN:
                            excelString += cell.getBooleanCellValue() + ", "
                            break
                        case Cell.CELL_TYPE_NUMERIC:
                            excelString += cell.getNumericCellValue() + ", "
                            break
                    }
                }
                excelString += "\n"
            }
            excelString += "\n"
        }

        workbook.close()
        byteIn.close()

        return excelString
    }

}
