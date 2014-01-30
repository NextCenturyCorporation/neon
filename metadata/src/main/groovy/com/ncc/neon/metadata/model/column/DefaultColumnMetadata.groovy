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

package com.ncc.neon.metadata.model.column

import groovy.transform.ToString



/**
 * Default implementation of metadata about a column.
 */

@ToString(includeNames = true)
class DefaultColumnMetadata implements ColumnMetadata{

    String databaseName
    String tableName
    String columnName
    boolean numeric
    boolean temporal
    boolean text
    boolean logical
    boolean object
    boolean array
    boolean nullable
    boolean heterogeneous

}
