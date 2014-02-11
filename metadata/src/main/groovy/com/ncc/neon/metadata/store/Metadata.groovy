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

import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata

/**
 * Metadata about widgets and data types
 */
interface Metadata {

    /**
     * Stores any data that a widget uses to initialize itself. This is specific to each widget.
     * @param data
     */
    void store(WidgetInitializationMetadata data)

    /**
     * Retrieves data that a widget may use to initialize itself
     * @param widgetName
     * @return
     */
    WidgetInitializationMetadata retrieve(String widgetName)

    /**
     * Stores data about a column that indicates data types in the column. This is useful for widgets that
     * can only handle certain types of data.
     * @param data
     */
    void store(ColumnMetadata data)

    /**
     * Retrieves any dataset information about the columns in the specified database/table. If columnNames
     * is empty or null, all columns will be returned
     * @param databaseName
     * @param tableName
     * @param columnNames
     * @return
     */
    ColumnMetadataList retrieve(String databaseName, String tableName, Set<String> columnNames)

    /**
     * Clears any metadata for the given table in the database
     * @param databseName
     * @param tableName
     */
    void clearColumnMetadata(String databseName, String tableName)

    /**
     * Stores information about default field values for widgets for a given dataset. This is useful for
     * pre-configuring mappings that will automatically be loaded when a widget starts up.
     * @param data
     */
    void store(WidgetAndDatasetMetadata data)

    /**
     * Retrieves the field mappings for the database/table/widget combination
     * @param databaseName
     * @param tableName
     * @param widgetName
     * @return
     */
    WidgetAndDatasetMetadataList retrieve(String databaseName, String tableName, String widgetName)

}
