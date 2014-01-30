package com.ncc.neon.metadata.store.script

import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.metadata.store.MetadataClearer
import com.ncc.neon.metadata.store.MetadataStorer
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 */

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
