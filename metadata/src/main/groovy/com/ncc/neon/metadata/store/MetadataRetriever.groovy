package com.ncc.neon.metadata.store
import com.mongodb.*
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.ActiveDatasetData
import com.ncc.neon.metadata.model.query.ColumnData
import com.ncc.neon.metadata.model.widget.WidgetInitializationData
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

/**
 * Retieves metadata from the MetadataConnection
 */

class MetadataRetriever {

    private final MongoObjectConverter converter
    private final MetadataConnection connection

    MetadataRetriever(MetadataConnection connection){
        this.connection = connection
        this.converter = new MongoObjectConverter()
    }

    List<WidgetInitializationData> retrieve(String widgetName){
        DBCollection collection = getCollection("widget")
        DBCursor cursor = collection.find(new BasicDBObject("widgetName", widgetName))
        return getAllData(cursor)
    }


    List<ColumnData> retrieve(String databaseName, String tableName){
        DBCollection collection = getCollection("column")
        DBCursor cursor = collection.find(new BasicDBObject(["databaseName": databaseName, "tableName": tableName]))
        return getAllData(cursor)

    }

    List<ActiveDatasetData> retrieve(String widgetName, String databaseName, String tableName){
        DBCollection collection = getCollection("dataset")
        DBCursor cursor = collection.find(new BasicDBObject(["widgetName": widgetName, "databaseName": databaseName, "tableName": tableName]))
        return getAllData(cursor)

    }

    private DBCollection getCollection(String name) {
        MongoClient mongo = connection.client
        DB database = mongo.getDB("metadata")
        DBCollection collection = database.createCollection(name, null)
        collection
    }

    private List getAllData(DBCursor cursor) {
        def data = []
        while (cursor.hasNext()) {
            DBObject object = cursor.next()
            data << converter.convertToObject(object)
        }
        return data
    }
}
