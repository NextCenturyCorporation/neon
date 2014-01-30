package com.ncc.neon.metadata.store
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata


/**
 * An api for storing metadata objects.
 */

class MetadataStorer {

    private final MongoObjectConverter converter
    private final def saveClosure

    MetadataStorer(MetadataConnection connection) {
        this.converter = new MongoObjectConverter()

        this.saveClosure = { String name, data ->
            DBObject document = converter.convertToMongo(data)

            MongoClient mongo = connection.client
            DB database = mongo.getDB(MetadataConstants.DATABASE)
            DBCollection widget = database.createCollection(name, null)

            widget.insert(document)
        }
    }

    void store(WidgetInitializationMetadata data) {
        saveClosure(MetadataConstants.WIDGET_TABLE, data)
    }

    void store(DefaultColumnMetadata data) {
        saveClosure(MetadataConstants.COLUMN_TABLE, data)
    }

    void store(WidgetAndDatasetMetadata data) {
        saveClosure(MetadataConstants.DATASET_TABLE, data)
    }

}
