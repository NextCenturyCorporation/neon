/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.sse

/**
 * Counts the records inside a given collection in a given database.
 */
interface RecordCounter {

    /**
     * Gets the number of records in a collection inside a database of the implementing class's designated type.
     * @param host The host where the collection to count can be found.
     * @param databaseName The name of the database holding the collection to count.
     * @param tableName The name of the collection to count.
     */
    long getCount(String host, String databaseName, String tableName)
}