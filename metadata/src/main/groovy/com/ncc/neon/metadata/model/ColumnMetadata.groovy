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

package com.ncc.neon.metadata.model

import groovy.transform.ToString
import org.codehaus.jackson.annotate.JsonIgnoreProperties

/**
 * Metadata about a column.
 */
@JsonIgnoreProperties(["databaseName","tableName"])
@ToString(includeNames = true)
class ColumnMetadata {

    String databaseName
    String tableName
    String columnName

    // the isXXX methods are needed so jackson doesn't throw an error when deserializing to json that it found conflicting
    // getter methods
    boolean numeric
    boolean isNumeric() { numeric }

    boolean temporal
    boolean isTemporal() { temporal }

    boolean text
    boolean isText() { text }

    boolean logical
    boolean isLogical() { logical }

    boolean object
    boolean isObject() { object }

    boolean array
    boolean isArray() { array }

    boolean nullable
    boolean isNullable() { nullable }

    boolean heterogeneous
    boolean isHeterogeneous() { heterogeneous }

}
