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

package com.ncc.neon.query

import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.result.Transform
import groovy.transform.ToString
import org.codehaus.jackson.annotate.JsonIgnoreProperties



/**
 * A query stores a filter for selecting data and optional aggregation methods for grouping the data.
 * The query is translated to a data source specific operation which returns the appropriate data.
 */
@ToString(includeNames = true)
@JsonIgnoreProperties(value = ['ignoreFilters_', 'selectionOnly_', 'ignoredFilterIds_'])
class Query {

    Filter filter
    boolean aggregatesArraysByElements = false
    boolean isDistinct = false
    List<String> fields = SelectClause.ALL_FIELDS
    List<AggregateClause> aggregates = []
    List<GroupByClause> groupByClauses = []
    List<SortClause> sortClauses = []
    LimitClause limitClause
    OffsetClause offsetClause
    Transform[] transforms

    def getDatabaseName() {
        filter.databaseName
    }

    def getTableName() {
        filter.tableName
    }

}
