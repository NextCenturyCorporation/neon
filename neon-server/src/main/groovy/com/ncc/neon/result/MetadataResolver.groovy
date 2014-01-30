package com.ncc.neon.result

import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata
import com.ncc.neon.metadata.store.MetadataRetriever
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.clauses.SelectClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Integration point between metadata and neon.
 * Provides methods to get the appropriate metadata.
 */

@Component
class MetadataResolver {

    @Autowired
    MetadataConnection metadataConnection

    /**
     * Gets all column data for a given dataset
     * @param databaseName The database name
     * @param tableName The table name
     * @return The column metadata
     */

    ColumnMetadataList resolveQuery(String databaseName, String tableName) {
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        return retriever.retrieve(databaseName, tableName, [])
    }

    /**
     * Gets column data based on fields from the query.
     * @param databaseName The database name
     * @param tableName The table name
     * @return The column metadata
     */

    ColumnMetadataList resolveQuery(Query query) {
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        List<String> columns = query.fields
        if (query.fields == SelectClause.ALL_FIELDS) {
            columns = []
        }
        return retriever.retrieve(query.databaseName, query.tableName, columns)
    }

    /**
     * Gets column data based on fields from the query group
     * @param databaseName The database name
     * @param tableName The table name
     * @return The column metadata
     */

    ColumnMetadataList resolveQuery(QueryGroup queryGroup) {
        def list = []
        queryGroup.queries.each { Query query ->
            ColumnMetadataList metadataList = resolveQuery(query)
            metadataList.dataSet.each { ColumnMetadata metadata ->
                if(!list.contains(metadata)){
                    list << metadata
                }
            }
        }
        return new ColumnMetadataList(list)
    }

    /**
     * Gets metadata for mapping element ids to column names
     * @param databaseName The database name
     * @param tableName The table name
     * @param widgetName An identifier for the widget
     * @return The metadata
     */

    WidgetAndDatasetMetadataList getInitializationData(String databaseName, String tableName, String widgetName){
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        return retriever.retrieve(databaseName, tableName, widgetName)
    }

    /**
     * Gets initialization data based on a client id
     * @param widget The identifier for the client
     * @return The initialization data.
     */

    WidgetInitializationMetadata getWidgetInitializationData(String widget) {
        MetadataRetriever retriever = new MetadataRetriever(metadataConnection)
        return retriever.retrieve(widget)
    }
}
