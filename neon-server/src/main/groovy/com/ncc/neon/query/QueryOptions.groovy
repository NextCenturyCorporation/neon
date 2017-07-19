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

package com.ncc.neon.query

/**
 * Options for modifying a query
 */
class QueryOptions {
    /** default options that applies filters to the query */
    public static final QueryOptions DEFAULT_OPTIONS = new QueryOptions()

    boolean ignoreFilters = false
    boolean selectionOnly = false

    //Used to refine a query.
    //Currently only used in a point_layer in the GTD's map, to limit elasticsearch's query to a visible region.
    String refinementSpecifier = null

    /** ignores these particular filters only (ignoreFilters takes precedence). this is useful if a visualization wants to ignore its own filters */
    Set<String> ignoredFilterIds = [] as HashSet
}
