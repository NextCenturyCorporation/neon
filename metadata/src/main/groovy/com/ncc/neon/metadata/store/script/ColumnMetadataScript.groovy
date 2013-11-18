package com.ncc.neon.metadata.store.script

import com.mongodb.*
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.metadata.store.MetadataClearer
import com.ncc.neon.metadata.store.MetadataStorer

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

class ColumnMetadataScript {

    private final List<String> dbExcludes = ["local", "metadata"]
    private final List<String> tableExcludes = ["system.indexes"]

    private final MetadataConnection connection = new MetadataConnection()
    final MetadataClearer clearer = new MetadataClearer(connection)
    final MetadataStorer storer = new MetadataStorer(connection)
    private final Map<String, DefaultColumnMetadata> metadataMap = [:]

    void executeScript() {
        MongoClient client = connection.client
        client.databaseNames.each { String databaseName ->
            if(dbExcludes.contains(databaseName)){
                return
            }
            DB db = client.getDB(databaseName)
            db.getCollectionNames().each { String tableName ->
                if(tableExcludes.contains(tableName)){
                    return
                }

                metadataMap.clear()
                insertMetadataIntoMap(databaseName, tableName)
                storeMetadata()
            }

        }
    }

    void storeMetadata() {
        metadataMap.each {k,v ->
            storer.store(v)
        }
    }

    private void insertMetadataIntoMap(String databaseName, String tableName) {
        MongoClient client = connection.client
        DB db = client.getDB(databaseName)
        DBCollection collection = db.createCollection(tableName, null)

        DBCursor cursor = collection.find()

        while (cursor.hasNext()) {
            DBObject object = cursor.next()
            handleRow(object, tableName, databaseName)
        }
        determineHeterogeneous()
    }

    private void determineHeterogeneous() {
        metadataMap.each{ k, v ->
            int count = 0
            v.properties.each { pk, pv ->
                if (pv == true && pk != "nullable") {
                    count++
                }
            }
            if(count > 1){
                v.heterogeneous = true
            }
        }
    }

    private void handleRow(DBObject object, String tableName, String databaseName) {
        object.keySet().each { String columnName ->
            if (!metadataMap.containsKey(columnName)) {
                metadataMap.put(columnName, new DefaultColumnMetadata(databaseName: databaseName, tableName: tableName, columnName: columnName))
            }
            def value = object.get(columnName)
            setMetadataValue(columnName, value)
        }
    }

    @SuppressWarnings("MethodSize")
    private void setMetadataValue(String columnName, def value) {
        DefaultColumnMetadata metadata = metadataMap.get(columnName)

        if (value == null) {
            metadata.nullable = true
        }
        else if (value instanceof Number) {
            metadata.numeric = true
        }
        else if (value instanceof String) {
            metadata.text = true
        }
        else if (value instanceof Date) {
            metadata.temporal = true
        }
        else if (value instanceof Boolean) {
            metadata.logical = true
        }
        else if (value instanceof List) {
            metadata.array = true
        }
        else {
            metadata.object = true
        }
    }

    public static void main(String [] args){
        ColumnMetadataScript script = new ColumnMetadataScript()
        script.clearer.dropColumnTable()
        script.executeScript()
    }

}
