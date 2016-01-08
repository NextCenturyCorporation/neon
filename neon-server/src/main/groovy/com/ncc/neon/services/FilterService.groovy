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

package com.ncc.neon.services

import com.ncc.neon.query.filter.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Service for working with a user's current filters
 */

@Component
@Path("/filterservice")
class FilterService {

    @Autowired
    GlobalFilterState filterState

    /**
     * Creates and returns an empty filter containing only the given database and table names
     * @param databaseName
     * @param tableName
     * @return
     */
    private Filter createEmptyFilter(String databaseName, String tableName) {
        return new Filter(databaseName: databaseName, tableName: tableName)
    }

    /**
     * Add a filter
     * @param filterKey The filter to add
     * @return an ADD filter event
     */
    @POST
    @Path("addfilter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    FilterEvent addFilter(FilterKey filterKey) {
        filterState.addFilter(filterKey)
        Filter added = filterKey.filter
        Filter removed = createEmptyFilter(added.databaseName, added.tableName)
        return new FilterEvent(type: "ADD", addedFilter: added, removedFilter: removed)
    }

    /**
     * Removes the filters associated with the specified id
     * @param filterId The id of the filter to remove
     * @return a REMOVE filter event
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removefilter")
    FilterEvent removeFilter(String id) {
        FilterKey filterKey = filterState.removeFilter(id)
        Filter removed = filterKey?.filter ?: createEmptyFilter("", "")
        Filter added = createEmptyFilter(removed.databaseName, removed.tableName)
        return new FilterEvent(type: "REMOVE", addedFilter: added, removedFilter: removed)
    }

    /**
     * Replace the filters for the given filter key. If none exists, this works the same as addFilter(filterKey)
     * @param filterKey The filter to replace
     * @return a REPLACE filter event
     */
    @POST
    @Path("replacefilter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    FilterEvent replaceFilter(FilterKey filterKey) {
        Filter removed = removeFilter(filterKey.id).removedFilter
        Filter added = addFilter(filterKey).addedFilter
        return new FilterEvent(type: "REPLACE", addedFilter: added, removedFilter: removed)
    }

    /**
     * Clears all filters.
     * @return a CLEAR filter event
     */
    @POST
    @Path("clearfilters")
    @Produces(MediaType.APPLICATION_JSON)
    FilterEvent clearFilters() {
        filterState.clearAllFilters()
        // use an empty dataset since the clear can span multiple datasets
        Filter empty = createEmptyFilter("", "")
        return new FilterEvent(type: "CLEAR", addedFilter: empty, removedFilter: empty)
    }

    /**
     * Get all filters for a given table.
     * @param tableName
     * @return
     */
    @GET
    @Path("filters/{databaseName}/{tableName}")
    @Produces(MediaType.APPLICATION_JSON)
    List getFilters(@PathParam("databaseName") String databaseName, @PathParam("tableName") String tableName) {
        if(databaseName == "*" && tableName == "*") {
            return filterState.getAllFilterKeys()
        } else if(databaseName == "*") {
            return filterState.getFilterKeysForTables(tableName)
        } else if(tableName == "*") {
            return filterState.getFilterKeysForDatabase(databaseName)
        }

        DataSet dataset = new DataSet(databaseName: databaseName, tableName: tableName)
        return filterState.getFilterKeysForDataset(dataset)
    }
}
