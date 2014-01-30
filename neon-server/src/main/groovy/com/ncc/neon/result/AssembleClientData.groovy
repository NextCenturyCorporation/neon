package com.ncc.neon.result
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.QueryResult


/**
 * Creates a client data object out of a query result and metadata.
 */

class AssembleClientData {

    QueryResult queryResult
    ColumnMetadataList columnMetadataList
    WidgetAndDatasetMetadataList initDataList

    /**
     * @return The query results and metadata packaged in an object
     */

    ClientData createClientData(){
        Map<String, Map<String, Boolean>> metadata = createMetadata(columnMetadataList)
        if(initDataList){
            Map<String, String> idToColumn = createInitData()
            return new InitializingClientData(data: queryResult.data, metadata: metadata, idToColumn: idToColumn)
        }

        new ClientData(data: queryResult.data, metadata: metadata)
    }

    private Map<String, String> createInitData() {
        Map initData = [:]
        initDataList.dataSet.each { WidgetAndDatasetMetadata init ->
            initData.put(init.elementId, init.value)
        }
        return initData
    }

    private Map<String, Map<String, Boolean>> createMetadata(ColumnMetadataList data) {
        Map<String, Map<String, Boolean>> metadata = [:]

        data.dataSet.each{ ColumnMetadata column ->
            Map map = createColumnMap(column)
            metadata.put(column.columnName, map)
        }

        return metadata
    }

    private Map<String, Boolean> createColumnMap(ColumnMetadata column) {
        Map<String, Boolean> map = [:]
        column.properties.each { k, v ->
            if (v == true) {
                map.put(k, v)
            }
        }
        return map
    }

}
