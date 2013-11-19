package com.ncc.neon.result
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.ListQueryResult
import org.junit.Test
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

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
        assemble.columnMetadataList = new ColumnMetadataList([new DefaultColumnMetadata(databaseName: "db", tableName: "table", temporal: true, columnName: "column1"),
                new DefaultColumnMetadata(databaseName: "db", tableName: "table", heterogeneous: true, columnName: "column2")])
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
