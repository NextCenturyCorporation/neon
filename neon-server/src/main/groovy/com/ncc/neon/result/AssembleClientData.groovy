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
