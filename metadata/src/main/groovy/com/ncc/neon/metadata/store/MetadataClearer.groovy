package com.ncc.neon.metadata.store

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.MongoClient
import com.ncc.neon.metadata.MetadataConnection



class MetadataClearer {

    private final def dropClosure

    MetadataClearer(MetadataConnection connection) {
        dropClosure = { String tableName ->
            MongoClient mongo = connection.client
            DB database = mongo.getDB(MetadataConstants.DATABASE)
            DBCollection collection = database.createCollection(tableName, null)
            collection.drop()
        }

    }

    void dropColumnTable() {
        dropClosure(MetadataConstants.COLUMN_TABLE)
    }

    void dropWidgetTable() {
        dropClosure(MetadataConstants.WIDGET_TABLE)
    }

    void dropDatasetTable() {
        dropClosure(MetadataConstants.DATASET_TABLE)
    }

}
