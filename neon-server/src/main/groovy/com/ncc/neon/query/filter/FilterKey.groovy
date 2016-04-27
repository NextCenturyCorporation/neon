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

package com.ncc.neon.query.filter

import groovy.transform.Canonical

import org.codehaus.jackson.annotate.JsonIgnoreProperties

/**
 * Stores a filter with its id
 */
@Canonical
@JsonIgnoreProperties(ignoreUnknown = true)
class FilterKey implements Serializable {

    private static final long serialVersionUID = -5783657018410727352L
    String id
    Filter filter

    DataSet getDataSet() {
        return new DataSet(databaseName: filter.databaseName, tableName: filter.tableName)
    }
}
