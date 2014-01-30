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

package com.ncc.neon.metadata.store
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.metadata.MetadataConnection
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadata
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.metadata.model.widget.WidgetInitializationMetadata


/**
 * An api for storing metadata objects.
 */

class MetadataStorer {

    private final MongoObjectConverter converter
    private final def saveClosure

    MetadataStorer(MetadataConnection connection) {
        this.converter = new MongoObjectConverter()

        this.saveClosure = { String name, data ->
            DBObject document = converter.convertToMongo(data)

            MongoClient mongo = connection.client
            DB database = mongo.getDB(MetadataConstants.DATABASE)
            DBCollection widget = database.createCollection(name, null)

            widget.insert(document)
        }
    }

    void store(WidgetInitializationMetadata data) {
        saveClosure(MetadataConstants.WIDGET_TABLE, data)
    }

    void store(DefaultColumnMetadata data) {
        saveClosure(MetadataConstants.COLUMN_TABLE, data)
    }

    void store(WidgetAndDatasetMetadata data) {
        saveClosure(MetadataConstants.DATASET_TABLE, data)
    }

}
