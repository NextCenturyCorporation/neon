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

package com.ncc.neon.userimport

import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException
import org.springframework.stereotype.Component
import javax.annotation.Resource

/**
 * Provides an easy means to obtain import helpers for various types of databases.
 */

@Component
class ImportHelperFactory {

    @Resource
    private ImportHelper mongoImportHelper

    /**
     * Gets the appropriate import helper for a given database type. Throws an exception if given an unsupported database type.
     * @param databaseType The type of database for which to get an import helper.
     * @return An import helper for the given database type.
     */
    ImportHelper getImportHelper(String databaseType) {
        switch (databaseType as DataSources) {
            case DataSources.mongo:
                return mongoImportHelper
            default:
                throw new NeonConnectionException("Import of user-defined data is unsupported for database type ${databaseType}")
        }
    }

}
