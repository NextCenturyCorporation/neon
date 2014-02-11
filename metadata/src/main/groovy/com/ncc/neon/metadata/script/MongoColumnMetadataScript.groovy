/*
 * Copyright 2014 Next Century Corporation
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
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.store.InMemoryMetadata
import com.ncc.neon.metadata.store.Metadata

/**
 * Generates metadata for all databases/tables in a mongo database
 *
 * This can be run from gradle by running
 * <pre>
 *     gradlew generateMongoDatabaseMetadata -PmetadataDatabase=&lt;database&gt; -PmetadataTable=&lt;table&gt; -PmetadataOutputFile=&lt;output-file&gt; -PmetadataHost=&lt;mongo-host&gt; -PmetadataPort=&lt;mongo-port&gt;
 * </pre>
 *
 * The host/port are optional and will default to localhost/27017
 */
class MongoColumnMetadataScript {

    private final Map<String, ColumnMetadata> metadataMap = [:]
    private final MongoClient mongo

    /**
     * Generates the metadata for a mongo database/table and writes it to a file
     * @param args
     * args[0] = The name of the mongo database to generate metadata for
     * args[1] = The name of the mongo table to generate metadata for
     * args[2] = The name of the file to write the metadata to
     * args[3] = (Optional) The host of the mongo instance (defaults to localhost)
     * args[4] = (Optional) The port of the mongo instance (defaults to 27017)
     */
    @SuppressWarnings('JavaIoPackageAccess') // this is run as a standalone script that accesses the file system
    public static void main(String[] args) {
        if (args.length < 3) {
            usage()
        }
        String database = args[0]
        String table = args[1]
        File file = new File(args[2])
        String host = "localhost"
        if (args.length > 3) {
            host = args[3]
        }
        int port = 27017
        if (args.length > 4) {
            port = args[4].toInteger()
        }
        new MongoColumnMetadataScript(host, port).generateMetadata(database, table, file)
    }

    private static void usage(args) {
        def builder = new StringBuilder()
        builder.append("Invalid arguments: ${args}")
        builder.append(System.getProperty("line.separator"))
        builder.append("usage: ${MongoColumnMetadataScript.name} <database> <table> <outputFile> [<mongoHost>] [<mongoPort>]")
        throw new IllegalArgumentException(builder.toString())
    }

    public MongoColumnMetadataScript(String mongoHost, int mongoPort) {
        this.mongo = new MongoClient(mongoHost, mongoPort)
    }

    /**
     * Generates metadata for the database/table in the mongo database and writes it
     * to the metadata file. If the file exists, the metadata will be replace.
     * If the file does not exist, it will be created.
     * @param database
     * @param table
     * @param metadataFile
     */
    void generateMetadata(String database, String table, File metadataFile) {
        InMemoryMetadata metadata = metadataFile.exists() ? InMemoryMetadata.create(metadataFile) : new InMemoryMetadata()
        storeMetadata(database, table, metadata)
        metadata.write(metadataFile)
    }

    private void storeMetadata(String database, String table, Metadata metadata) {
        metadata.clearColumnMetadata(database, table)
        metadataMap.clear()
        insertMetadataIntoMap(database, table)
        storeMetadataMap(metadata)
    }


    private void storeMetadataMap(Metadata metadata) {
        metadataMap.each { k, v ->
            metadata.store(v)
        }
    }

    private void insertMetadataIntoMap(String databaseName, String tableName) {
        DB db = mongo.getDB(databaseName)
        DBCollection collection = db.getCollection(tableName)
        DBCursor cursor = collection.find()

        while (cursor.hasNext()) {
            DBObject object = cursor.next()
            handleRow(object, tableName, databaseName)
        }
        determineHeterogeneous()
    }

    private void determineHeterogeneous() {
        metadataMap.each { k, v ->
            int count = 0
            v.properties.each { pk, pv ->
                if (pv == true && pk != "nullable") {
                    count++
                }
            }
            if (count > 1) {
                v.heterogeneous = true
            }
        }
    }

    private void handleRow(DBObject object, String tableName, String databaseName) {
        object.keySet().each { String columnName ->
            if (!metadataMap.containsKey(columnName)) {
                metadataMap.put(columnName, new ColumnMetadata(databaseName: databaseName, tableName: tableName, columnName: columnName))
            }
            def value = object.get(columnName)
            setMetadataValue(columnName, value)
        }
    }

    private void setMetadataValue(String columnName, def value) {
        ColumnMetadata metadata = metadataMap.get(columnName)
        if (value == null) {
            metadata.nullable = true
        } else if (value instanceof Number) {
            metadata.numeric = true
        } else if (value instanceof String) {
            metadata.text = true
        } else if (value instanceof Date) {
            metadata.temporal = true
        } else if (value instanceof Boolean) {
            metadata.logical = true
        } else if (value instanceof List) {
            metadata.array = true
        } else {
            metadata.object = true
        }
    }

}
