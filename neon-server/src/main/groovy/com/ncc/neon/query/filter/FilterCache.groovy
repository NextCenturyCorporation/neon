package com.ncc.neon.query.filter

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.WhereClause

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

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

/**
 * Holds filters for a give filter key in memory.
 */
class FilterCache implements Serializable {

    private static final long serialVersionUID = -757738218779210868L

    ConcurrentMap<String, FilterKey> cache = new ConcurrentHashMap<String, FilterKey>()

    /**
     * Clears all existing filters
     */
    void clearAllFilters() {
        cache.clear()
    }

    /**
     * Adds a filter to the filter state. If the cache already contains a filter with the same key, they will be
     * merged with an AND clause, and this container will be modified to reflect the new filter.
     * @param filterKey
     */
    void addFilter(FilterKey filterKey) {
        def oldFilterKey = cache.putIfAbsent(filterKey.id, filterKey)
        if (oldFilterKey) {
            filterKey.filter.whereClause = determineFilterWhereClause(oldFilterKey.filter, filterKey.filter)
            cache.replace(filterKey.id, filterKey)
        }
    }

    private WhereClause determineFilterWhereClause(Filter oldFilter, Filter filter) {
        if (oldFilter.whereClause) {
            AndWhereClause andWhereClause = new AndWhereClause(whereClauses: [oldFilter.whereClause, filter.whereClause])
            return andWhereClause
        }
        return filter.whereClause
    }

    /**
     * Removes the filter
     * @param id The id of the filter generated when adding it
     * @return
     */
    FilterKey removeFilter(String id) {
        return cache.remove(id)
    }

    /**
     * Gets any filters that are applied to the specified dataset
     * @param dataset The current dataset
     * @return A list of filters applied to that dataset
     */
    List<Filter> getFiltersForDataset(DataSet dataSet) {
        return cache.findResults { k, v ->
            if (v.dataSet == dataSet) {
                return v.filter
            }
        }
    }

    /**
     * Gets the filter keys (filter + id) for the specified dataset. This is similar to getFiltersForDataset except
     * that it returns the whole filter key
     * @param dataSet
     * @return
     */
    List<FilterKey> getFilterKeysForDataset(DataSet dataSet) {
        return cache.findResults { k, v ->
            if (v.dataSet == dataSet) {
                return v
            }
        }
    }

}
