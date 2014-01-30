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

package com.ncc.neon.metadata.store.script

import com.mongodb.*
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.metadata.store.MetadataClearer
import com.ncc.neon.metadata.store.MetadataStorer



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
