package com.ncc.neon.result

import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.query.QueryResult
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
        AssembleClientData clientData = new AssembleClientData()
        clientData.metadataObject = new ColumnMetadataList([])
        clientData.queryResult = [toJson:{"[]"}] as QueryResult
        assert "{\"data\":[],\"metadata\":[]}" == clientData.createClientData()
    }

    @Test
    void testEmptyWidgetMetadata() {
        AssembleClientData clientData = new AssembleClientData()
        clientData.metadataObject = new WidgetAndDatasetMetadataList([])
        clientData.queryResult = [toJson:{"[]"}] as QueryResult
        assert "{\"data\":[],\"metadata\":[]}" == clientData.createClientData()
    }

    @Test
    void testWidgetMetadata() {
        def widgetData = new WidgetAndDatasetMetadataList([new WidgetAndDatasetMetadata(databaseName: "db", tableName: "table", widgetName: "widget", elementId: "date", value: "created_at"),
                new WidgetAndDatasetMetadata(databaseName: "db", tableName: "table", widgetName: "timeline", elementId: "y", value: "value")])

        AssembleClientData clientData = new AssembleClientData()
        clientData.metadataObject = widgetData
        clientData.queryResult = [toJson:{"[]"}] as QueryResult

        assert "{\"data\":[],\"metadata\":[{\"elementId\":\"date\",\"value\":\"created_at\"},{\"elementId\":\"y\",\"value\":\"value\"}]}" == clientData.createClientData()
    }

    @Test
    void testColumnMetadata() {
        def columnData = new ColumnMetadataList([new DefaultColumnMetadata(databaseName: "db", tableName: "table", temporal: true, columnName: "column1"),
                new DefaultColumnMetadata(databaseName: "db", tableName: "table", heterogeneous: true, columnName: "column2")])

        AssembleClientData clientData = new AssembleClientData()
        clientData.metadataObject = columnData
        clientData.queryResult = [toJson:{"[]"}] as QueryResult

        assert "{\"data\":[],\"metadata\":[{\"temporal\":true,\"columnName\":\"column1\"},{\"columnName\":\"column2\",\"heterogeneous\":true}]}" == clientData.createClientData()
    }
}
