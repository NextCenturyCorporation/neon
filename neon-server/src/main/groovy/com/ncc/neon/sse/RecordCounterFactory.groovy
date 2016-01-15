/*
 * Copyright 2016 Next Century Corporation
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

import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException

import org.springframework.stereotype.Component
import javax.annotation.Resource

/**
 * Factory for RecordCounters for various databases.
 */
@Component
class RecordCounterFactory {

    @Resource
    private RecordCounter mongoRecordCounter

    @Resource
    private RecordCounter elasticSearchRecordCounter

    /**
     * Returns a RecordCounter for the given database type, or throws an error if one does not exist.
     * @param databaseType The type of database to get a RecordCounter for.
     * @return A RecordCounter for the given database type.
     */
    RecordCounter getRecordCounter(String databaseType) {
        switch (databaseType as DataSources) {
            case DataSources.mongo:
                return mongoRecordCounter
            case DataSources.elasticsearch:
                return elasticSearchRecordCounter
            default:
                throw new NeonConnectionException("There is no RecordCounter currently implemented for database type ${databaseType}")
        }
    }
}