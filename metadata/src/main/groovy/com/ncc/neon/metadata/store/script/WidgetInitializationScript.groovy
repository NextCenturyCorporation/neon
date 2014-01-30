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
