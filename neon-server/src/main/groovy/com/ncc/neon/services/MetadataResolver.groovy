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

package com.ncc.neon.services

import com.ncc.neon.metadata.model.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.WidgetInitializationMetadata
import com.ncc.neon.metadata.store.InMemoryMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

/**
 * Integration point between metadata and neon.
 * Provides methods to get the appropriate metadata.
 */

@Component
class MetadataResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataResolver)
    private static final METADATA_FILE_NAME = System.getProperty("metadata.file","Metadata.groovy")

    @Delegate
    @Autowired
    InMemoryMetadata metadata

    @SuppressWarnings("JavaIoPackageAccess") // metadata is loaded from a file on the classpath
    @PostConstruct
    void loadMetadata() {
        URL url = getClass().getResource("/${METADATA_FILE_NAME}")
        if ( url ) {
            File file = new File(url.toURI())
            LOGGER.info("Loading metadata from ${file}")
            metadata.load(file)
        }
        else {
            LOGGER.info("No ${METADATA_FILE_NAME} file found on classpath")
        }
    }

    /**
     * Gets metadata for mapping element ids to column names
     * @param databaseName The database name
     * @param tableName The table name
     * @param widgetName An identifier for the widget
     * @return The metadata
     */

    List<WidgetAndDatasetMetadata> getWidgetDatasetData(String databaseName, String tableName, String widgetName) {
        return metadata.retrieve(databaseName, tableName, widgetName)
    }

    /**
     * Gets initialization data based on a client id
     * @param widget The identifier for the client
     * @return The initialization data.
     */

    WidgetInitializationMetadata getWidgetInitializationData(String widget) {
        return metadata.retrieve(widget)
    }
}
