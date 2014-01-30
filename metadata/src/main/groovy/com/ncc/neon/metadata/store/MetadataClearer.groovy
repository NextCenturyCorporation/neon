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
import com.mongodb.MongoClient
import com.ncc.neon.metadata.MetadataConnection



class MetadataClearer {

    private final def dropClosure

    MetadataClearer(MetadataConnection connection) {
        dropClosure = { String tableName ->
            MongoClient mongo = connection.client
            DB database = mongo.getDB(MetadataConstants.DATABASE)
            DBCollection collection = database.createCollection(tableName, null)
            collection.drop()
        }

    }

    void dropColumnTable() {
        dropClosure(MetadataConstants.COLUMN_TABLE)
    }

    void dropWidgetTable() {
        dropClosure(MetadataConstants.WIDGET_TABLE)
    }

    void dropDatasetTable() {
        dropClosure(MetadataConstants.DATASET_TABLE)
    }

}
