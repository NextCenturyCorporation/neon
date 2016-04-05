/*
 * Copyright 2016 Next Century Corporation
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

import com.ncc.neon.query.result.TabularQueryResult

import com.ncc.neon.query.export.ExportBundle
import com.ncc.neon.query.export.ExportQueryRequest
import com.ncc.neon.query.export.ExportField

import com.ncc.neon.query.Query

import com.ncc.neon.connect.DataSources

import org.json.JSONObject

import javax.ws.rs.core.Response

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

import org.apache.poi.xssf.usermodel.XSSFWorkbook

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ExportServiceTest {

    private ExportService exportService

    private static final String HOST = "aHost"
    private static final String DATABASE_TYPE = DataSources.mongo.toString()
    private static final String RESULT_CSV_STRING1 = '"_id","""first"" name","last name","age","location",\n"5137b623a9f279d831b6fb86","""Bill""","Jones","null","[type:Point, coordinates:[20.0, 12.0]]",\n"5137b623a9f279d831b6fb8c","","Bowles","36","[type:Point, coordinates:[11.0, -23.0]]",\n'
    private static final String RESULT_CSV_STRING2 = '"_id","firstname","lastname","location","age",\n"5137b623a9f279d831b6fb86","""Bill""","Jones","[type:Point, coordinates:[20.0, 12.0]]","null",\n"5137b623a9f279d831b6fb8c","","Bowles","[type:Point, coordinates:[11.0, -23.0]]","36",\n'
    private static final String RESULT_CSV_STRING3 = '"key","count",\n"Jones","1",\n"Bowles","1",\n'
    private static final String RESULT_EXCEL_STRING1 = 'Key, Count, \nJones, 1, \nBowles, 1, \n\n'
    private static final String RESULT_EXCEL_STRING2 = 'key, count, \nJones, 1, \nBowles, 1, \n\n'
    private static final String RESULT_EXCEL_STRING3 = 'Key, Count, \nJones, 1, \nBowles, 1, \n\n_id, firstname, lastname, location, age, \n5137b623a9f279d831b6fb86, "Bill", Jones, [type:Point, coordinates:[20.0, 12.0]], null, \n5137b623a9f279d831b6fb8c, , Bowles, [type:Point, coordinates:[11.0, -23.0]], 36, \n\n'
    private static final List TABULAR_QUERY_RESULT = [
        [
            "_id": "5137b623a9f279d831b6fb86",
            "firstname": "\"Bill\"",
            "lastname": "Jones",
            "location": [
                "type": "Point",
                "coordinates": [20.0 , 12.0]
            ]
        ],
        [
            "_id": "5137b623a9f279d831b6fb8c",
            "firstname": "",
            "lastname": "Bowles",
            "age": 36,
            "location": [
                "type": "Point",
                "coordinates": [11.0, -23.0]
            ]
        ]
    ]

    @Before
    void setup() {
        exportService = new ExportService()
        exportService.queryService = [
            executeQuery: { host, databaseType, ignoreFilters, selectionOnly, ignoredFilteredIds, query ->
                return new TabularQueryResult(TABULAR_QUERY_RESULT)
            }
        ] as QueryService
    }

    @Test
    void "make export data test"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest()])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        assert obj.data
        assert exportService.ID_TO_DATA_PAIRS[obj.data]
        assert exportService.ID_TO_DATA_PAIRS[obj.data].host == HOST
        assert exportService.ID_TO_DATA_PAIRS[obj.data].databaseType == DATABASE_TYPE
        assert exportService.ID_TO_DATA_PAIRS[obj.data].bundle == bundle
    }

    @Test
    void "execute export test for csv file with field names list"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest(
            query: new Query(),
            name: "test-export-1",
            fields: [
                new ExportField(query: "_id", pretty: "_id"),
                new ExportField(query: "firstname", pretty: "\"first\" name"),
                new ExportField(query: "lastname", pretty: "last name"),
                new ExportField(query: "age", pretty: "age"),
                new ExportField(query: "location", pretty: "location")
            ],
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
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))
        byte[] buffer = new byte[235]
        ZipEntry entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-1.csv"
        zipIn.read(buffer)
        assert new String(buffer, "UTF-8") == RESULT_CSV_STRING1
        zipIn.close()
    }

    @Test
    void "execute export test for csv file with no field names list"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest(
            query: new Query(),
            name: "test-export-1",
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
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))
        byte[] buffer = new byte[229]
        ZipEntry entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-1.csv"
        zipIn.read(buffer)
        assert new String(buffer, "UTF-8") == RESULT_CSV_STRING2
        zipIn.close()
    }

    // TODO Add a second export query request to the data in the export bundle.
    @Ignore
    @Test
    @SuppressWarnings('MethodSize')
    void "execute export test for multiple csv files"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 0, data: [new ExportQueryRequest(
            query: new Query(),
            name: "test-export-1",
            fields: [
                new ExportField(query: "_id", pretty: "_id"),
                new ExportField(query: "firstname", pretty: "\"first\" name"),
                new ExportField(query: "lastname", pretty: "last name"),
                new ExportField(query: "age", pretty: "age"),
                new ExportField(query: "location", pretty: "location")
            ],
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
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))

        byte[] buffer1 = new byte[235]
        ZipEntry entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-1.csv"
        zipIn.read(buffer1)
        assert new String(buffer1, "UTF-8") == RESULT_CSV_STRING1

        byte[] buffer2 = new byte[42]
        entry = zipIn.getNextEntry()
        assert entry.getName() == "test-export-2.csv"
        zipIn.read(buffer2)
        assert new String(buffer2, "UTF-8") == RESULT_CSV_STRING3

        zipIn.close()
    }

    // TODO Add an export query request to the data in the export bundle.
    @Ignore
    @Test
    void "execute export test for excel file with field names list"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 1, data: [])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        Response response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.toByteArray())
        ZipInputStream zipIn = new ZipInputStream(byteIn)
        assert getExcelSheetAsString(zipIn) == RESULT_EXCEL_STRING1
        zipIn.close()
        byteIn.close()
    }

    // TODO Add an export query request to the data in the export bundle.
    @Ignore
    @Test
    void "execute export test for excel file with no field names list"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 1, data: [])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        Response response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.toByteArray())
        ZipInputStream zipIn = new ZipInputStream(byteIn)
        assert getExcelSheetAsString(zipIn) == RESULT_EXCEL_STRING2
        zipIn.close()
        byteIn.close()
    }

    // TODO Add a second export query request to the data in the export bundle.
    @Ignore
    @Test
    void "execute export test for excel file with multiple sheets"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 1, data: [new ExportQueryRequest(
            query: new Query(),
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
        assert getExcelSheetAsString(zipIn) == RESULT_EXCEL_STRING3
        zipIn.close()
        byteIn.close()
    }

    @Test
    void "execute export test with unknown id"() {
        Response response = exportService.executeExport("1234")
        assert response.getStatus() == 400
    }

    @Test
    void "execute export test with unknown file type"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 3, data: [new ExportQueryRequest(
            query: new Query(),
            name: "test-export-1",
            fields: [
                new ExportField(query: "_id", pretty: "_id"),
                new ExportField(query: "firstname", pretty: "\"first\" name"),
                new ExportField(query: "lastname", pretty: "last name"),
                new ExportField(query: "age", pretty: "age"),
                new ExportField(query: "location", pretty: "location")
            ],
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
        ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))
        byte[] buffer = new byte[235]
        ZipEntry entry = zipIn.getNextEntry()
        // Should default to csv file if unknown file type
        assert entry.getName() == "test-export-1.csv"
        zipIn.read(buffer)
        assert new String(buffer, "UTF-8") == RESULT_CSV_STRING1
        zipIn.close()
    }

    @Test
    void "execute export test with no data"() {
        ExportBundle bundle = new ExportBundle(name: "test", fileType: 1, data: [])
        String result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        JSONObject obj = new JSONObject(result)
        Response response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ByteArrayInputStream byteIn = new ByteArrayInputStream(out.toByteArray())
        ZipInputStream zipIn = new ZipInputStream(byteIn)
        assert getExcelSheetAsString(zipIn) == ""
        zipIn.close()
        byteIn.close()

        bundle = new ExportBundle(name: "test", fileType: 0, data: [])
        result = exportService.makeExportData(HOST, DATABASE_TYPE, bundle)
        obj = new JSONObject(result)
        response = exportService.executeExport(obj.data)
        assert response.getStatus() == 200
        out = new ByteArrayOutputStream()
        response.getEntity().write(out)
        ZipInputStream zipIn2 = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))
        assert zipIn2.getNextEntry() == null
        zipIn2.close()
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
