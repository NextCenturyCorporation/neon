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

package com.ncc.neon.result

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.ListQueryResult
import org.junit.Test


class AssembleClientDataTest {

    @Test
    void testEmptyColumnMetadata() {
        AssembleClientData assemble = new AssembleClientData()
        assemble.columnMetadataList = new ColumnMetadataList([])
        assemble.queryResult = new ListQueryResult()
        ClientData clientData = assemble.createClientData()
        assert clientData
        assert !clientData.data
        assert !clientData.metadata
    }

    @Test
    void testEmptyWidgetMetadata() {
        AssembleClientData assemble = new AssembleClientData()
        assemble.columnMetadataList = new ColumnMetadataList([])
        assemble.queryResult = new ListQueryResult()
        assemble.initDataList = new WidgetAndDatasetMetadataList([])
        InitializingClientData clientData = assemble.createClientData()
        assert clientData
        assert !clientData.data
        assert !clientData.metadata
        assert !clientData.idToColumn
    }

    @Test
    void testColumnMetadata() {
        def data = ["data1", "data2", "data3"]

        AssembleClientData assemble = new AssembleClientData()
        assemble.columnMetadataList = new ColumnMetadataList([new ColumnMetadata(databaseName: "db", tableName: "table", temporal: true, columnName: "column1"),
                new ColumnMetadata(databaseName: "db", tableName: "table", heterogeneous: true, columnName: "column2")])
        assemble.queryResult = new ListQueryResult(data)

        ClientData clientData = assemble.createClientData()
        assert clientData.data
        assert clientData.data == data
        assert clientData.metadata
        assert clientData.metadata == [column1:[temporal:true], column2:[heterogeneous:true]]

    }

    @Test
    void testWidgetMetadata() {
        def widgetData = new WidgetAndDatasetMetadataList([new WidgetAndDatasetMetadata(databaseName: "db", tableName: "table", widgetName: "widget", elementId: "date", value: "created_at"),
                new WidgetAndDatasetMetadata(databaseName: "db", tableName: "table", widgetName: "timeline", elementId: "y", value: "value")])

        AssembleClientData assemble = new AssembleClientData()
        assemble.columnMetadataList = new ColumnMetadataList([])
        assemble.queryResult = new ListQueryResult()
        assemble.initDataList = widgetData

        InitializingClientData clientData = assemble.createClientData()
        assert clientData.idToColumn
        assert clientData.idToColumn.size() == 2
        assert clientData.idToColumn == [date:"created_at", y:"value"]
    }
}
