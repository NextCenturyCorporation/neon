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

package com.ncc.neon.metadata.store.script

import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.store.MetadataClearer
import com.ncc.neon.metadata.store.MetadataStorer


/**
 * This script inserts data into the dataset metadata table from the content of bundle.csv
 * in src/main/resources. Eventually, this will be replaced by a UI that allows a user to configure
 * their dataset configuration metadata.
 */

class DatasetMetadataScript {

    private final List<WidgetAndDatasetMetadataList> metadataList = []
    private final MetadataConnection connection = new MetadataConnection()
    final MetadataClearer clearer = new MetadataClearer(connection)
    final MetadataStorer storer = new MetadataStorer(connection)

    @SuppressWarnings("ThrowRuntimeException")
    void executeScript(){
        String text = DatasetMetadataScript.classLoader.getResourceAsStream("bundle.csv")?.text
        if(!text){
            throw new RuntimeException("bundle.csv does not exist")
        }
        readEachLine(text)
        outputFile()
    }

    private void readEachLine(String text) {
        text.split("\n").each { String line ->
            if (shouldIgnoreLine(line)) {
                return
            }
            def dataArray = line.split(",")
            populateConfigurationMapping(dataArray)
        }
    }

    private void outputFile() {
        metadataList.each {
            storer.store(it)
        }
    }

    private void populateConfigurationMapping(String[] dataArray) {
        validateLine(dataArray)

        def columnMap = parseColumnMap(dataArray[3])

        columnMap.each{ k,v ->
            WidgetAndDatasetMetadata metadata = new WidgetAndDatasetMetadata(databaseName: dataArray[0], tableName: dataArray[1], widgetName: dataArray[2], elementId: k, value: v)
            metadataList << metadata
        }
    }

    private def parseColumnMap(String mapString) {
        String mapData = mapString.trim()[1..-2]
        String [] keyValues = mapData.split(";")
        def columnMap = [:]
        keyValues.each { String keyValue ->
            String [] split = keyValue.split(":")
            columnMap.put(split[0],split[1])
        }
        return columnMap
    }

    private void validateLine(String[] dataArray) {
        if (dataArray.length != 4) {
            throw new IllegalArgumentException("The spec file is malformed: ${dataArray}")
        }
    }

    private boolean shouldIgnoreLine(String line) {
        line.trim().length() == 0 || line.startsWith("#")
    }

    public static void main(String [] args){
        DatasetMetadataScript script = new DatasetMetadataScript()
        script.clearer.dropDatasetTable()
        script.executeScript()
    }
}
