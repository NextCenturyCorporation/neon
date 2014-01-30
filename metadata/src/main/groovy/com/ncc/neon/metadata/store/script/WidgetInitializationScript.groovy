package com.ncc.neon.metadata.store.script

import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata
import com.ncc.neon.metadata.store.MetadataClearer
import com.ncc.neon.metadata.store.MetadataStorer



class WidgetInitializationScript {

    final MetadataConnection connection = new MetadataConnection()
    final MetadataClearer clearer = new MetadataClearer(connection)
    final MetadataStorer storer = new MetadataStorer(connection)

    void executeScript(){
        WidgetInitializationMetadata widgetInitializationMetadata = new WidgetInitializationMetadata()
        widgetInitializationMetadata.widgetName = "QueryBuilder"
        widgetInitializationMetadata.initDataJson = '{"query":"use mydb; select * from sample;"}'
        storer.store(widgetInitializationMetadata)
    }

    public static void main(String [] args){
        WidgetInitializationScript script = new WidgetInitializationScript()
        script.clearer.dropWidgetTable()
        script.executeScript()
    }
}
