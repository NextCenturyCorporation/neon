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

package com.ncc.neon.metadata.store

import com.ncc.neon.metadata.model.ColumnMetadata
import com.ncc.neon.metadata.model.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.WidgetInitializationMetadata
import org.springframework.stereotype.Component

/**
 * An implementation of the metadata that stores all metadata in memory. The write/load methods can be used to
 * write the metadata to a file and load it back

 */
@Component
class InMemoryMetadata implements Metadata {

    /** a mapping of a widget name to any metadata used to initialize that widget */
    private final Map<String, WidgetInitializationMetadata> stringWidgetInitializationMetadata

    /** a mapping of a database name to a map of table/column metadata for the tables in that database */
    private final Map<String, Map<String, Map<String, ColumnMetadata>>> columnMetadata

    /**
     * a mapping of a database/table/widgetName to the selectors that indicate which elements should have
     * which values in that widget
     */
    private final Map<String, Map<String, Map<String, Map<String, WidgetAndDatasetMetadata>>>> widgetAndDatasetMetadata

    InMemoryMetadata() {
        // use a linked hashmap so printout is always in the same order
        stringWidgetInitializationMetadata = ([:] as LinkedHashMap).withDefault { widgetName -> new WidgetInitializationMetadata(widgetName: widgetName) }
        columnMetadata = ([:] as LinkedHashMap).withDefault { databaseName -> ([:] as LinkedHashMap).withDefault { tableName -> [:] as LinkedHashMap } }
        widgetAndDatasetMetadata = ([:] as LinkedHashMap).withDefault { databaseName -> ([:] as LinkedHashMap).withDefault { tableName -> ([:] as LinkedHashMap).withDefault { widgetName -> [:] as LinkedHashMap} } }
    }

    @Override
    void store(WidgetInitializationMetadata data) {
        stringWidgetInitializationMetadata[data.widgetName] = data
    }

    @Override
    WidgetInitializationMetadata retrieve(String widgetName) {
        return stringWidgetInitializationMetadata[widgetName]
    }

    @Override
    void store(ColumnMetadata data) {
        columnMetadata[data.databaseName][data.tableName][data.columnName] = data
    }

    @Override
    List<ColumnMetadata> retrieve(String databaseName, String tableName, Set<String> columnNames) {

        List<ColumnMetadata> metadata = columnMetadata[databaseName][tableName].values() as List

        if (columnNames) {
            // only keep the columns that match the column we're interested in
            metadata = metadata.findAll { columnNames.contains(it.getColumnName()) }
        }

        return metadata
    }

    @Override
    void clearColumnMetadata(String databseName, String tableName) {
        columnMetadata[databseName][tableName].clear()
    }

    @Override
    void store(WidgetAndDatasetMetadata data) {
        widgetAndDatasetMetadata[data.databaseName][data.tableName][data.widgetName][data.elementId] = data
    }

    @Override
    List<WidgetAndDatasetMetadata> retrieve(String databaseName, String tableName, String widgetName) {
        return widgetAndDatasetMetadata[databaseName][tableName][widgetName].values() as List
    }

    /**
     * Writes this metadata to a file
     * @param file
     */
    void write(File file) {
        Map<String, Object> config = [:] as LinkedHashMap
        config["init"] = stringWidgetInitializationMetadata
        config["columns"] = columnMetadata
        config["widgets"] = widgetAndDatasetMetadata
        // the database/table/widget/element/column are encoded as part of the nested structure and do not need to be
        // explicitly written
        file.text = new ConfigWriter(['databaseName', 'tableName', 'widgetName', 'elementId', 'columnName'] as Set).writeConfig("metadata", config)
    }

    /**
     * Creates metadata from a file (either saved from {@link #write(java.io.File)} or manually created
     * @param file
     * @return
     */
    static InMemoryMetadata create(File file) {
        InMemoryMetadata metadata = new InMemoryMetadata()
        metadata.load(file)
        return metadata
    }

    /**
     * Loads this metadata from a file
     * @param file
     */
    void load(File file) {
        def config = new ConfigSlurper().parse(file.text)["metadata"]
        populateWidgetInitializationMetadata(config)
        populateColumnMetadata(config)
        populateWidgetAndDatasetMetadata(config)
    }

    private void populateWidgetInitializationMetadata(ConfigObject config) {
        config["init"].each { name, saved ->
            WidgetInitializationMetadata initData = new WidgetInitializationMetadata(widgetName: name)
            copyProperties(saved, initData)
            store(initData)
        }
    }

    private void populateColumnMetadata(ConfigObject config) {
        config["columns"].each { dbName, tables ->
            tables.each { table, columnMappings ->
                columnMappings.each { columnName, fields ->
                    ColumnMetadata columnData = createColumnMetadata(dbName, table, columnName, fields)
                    store(columnData)
                }
            }
        }
    }

    private static ColumnMetadata createColumnMetadata(databaseName, tableName, columnName, fields) {
        ColumnMetadata columnData = new ColumnMetadata(databaseName: databaseName, tableName: tableName, columnName: columnName)
        copyProperties(fields, columnData)
        return columnData

    }

    private void populateWidgetAndDatasetMetadata(ConfigObject config) {
        config["widgets"].each { dbName, tables ->
            tables.each { tableName, widgetMappings ->
                widgetMappings.each { widgetName, widgetMapping ->
                    widgetMapping.each { elementId,  data ->
                        WidgetAndDatasetMetadata widgetData = createWidgetAndDatasetMetadata(dbName, tableName, widgetName, elementId, data)
                        store(widgetData)
                    }
                }
            }
        }
    }

    private static WidgetAndDatasetMetadata createWidgetAndDatasetMetadata(databaseName, tableName, widgetName, elementId, widgetData) {
        WidgetAndDatasetMetadata widgetMetaData = new WidgetAndDatasetMetadata(databaseName: databaseName,
                tableName: tableName, widgetName: widgetName, elementId: elementId)
        copyProperties(widgetData, widgetMetaData)
        return widgetMetaData
    }

    private static void copyProperties(def from, def to) {
        from.each { prop, val ->
            to.setProperty(prop, val)
        }
    }

}
