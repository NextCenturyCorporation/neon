package com.ncc.neon.metadata.store
import com.mongodb.*
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata


/**
 * Retieves metadata from the MetadataConnection
 */

class MetadataRetriever {

    private final MongoObjectConverter converter
    private final MetadataConnection connection

    MetadataRetriever(MetadataConnection connection) {
        this.connection = connection
        this.converter = new MongoObjectConverter()
    }

    WidgetInitializationMetadata retrieve(String widgetName) {
        DBCollection collection = getCollection(MetadataConstants.WIDGET_TABLE)
        DBObject object = collection.findOne(new BasicDBObject("widgetName", widgetName))
        if(!object){
            return new WidgetInitializationMetadata(widgetName: widgetName)
        }

        return converter.convertToObject(object)
    }

    ColumnMetadataList retrieve(String databaseName, String tableName, List<String> columnNames) {
        DBCollection collection = getCollection(MetadataConstants.COLUMN_TABLE)
        DBCursor cursor = collection.find(new BasicDBObject(["databaseName": databaseName, "tableName": tableName]))
        List<ColumnMetadata> columnData = getAllData(cursor)
        if (!columnNames) {
            return new ColumnMetadataList(columnData)
        }

        def filteredColumnData = columnData.findAll {
            columnNames.contains(it.columnName)
        }
        return new ColumnMetadataList(filteredColumnData)
    }

    WidgetAndDatasetMetadataList retrieve(String databaseName, String tableName, String widgetName) {
        DBCollection collection = getCollection(MetadataConstants.DATASET_TABLE)
        DBCursor cursor = collection.find(new BasicDBObject(["widgetName": widgetName, "databaseName": databaseName, "tableName": tableName]))
        return new WidgetAndDatasetMetadataList(getAllData(cursor))
    }

    private DBCollection getCollection(String name) {
        MongoClient mongo = connection.client
        DB database = mongo.getDB("metadata")
        return database.createCollection(name, null)
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
