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
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType



/**
 * Service for working with a user's current filters
 */

@Component
@Path("/filterservice")
class FilterService {

    @Autowired
    FilterState filterState

    /**
     * Creates a filter key which is used in other operations in this service. A client needs
     * a unique filter key per dataset per widget instance. This is how we keep track of which widget
     * instance applied which filter or selection.
     * @param dataSet The dataset to which this filter key should be applied.
     * @return An object that serializes filter key contents to the client.
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("registerforfilterkey")
    FilterEvent registerForFilterKey(DataSet dataSet) {
        FilterKey filterKey = new FilterKey(uuid: UUID.randomUUID(), dataSet: dataSet)
        FilterEvent.fromFilterKey(filterKey)
    }

    /**
     * Add an additional filter
     * @param container An object containing a filter key, a server generated identifier for a given widget instance,
     * and a filter,
     */
    @POST
    @Path("addfilter")
    @Consumes(MediaType.APPLICATION_JSON)
    void addFilter(FilterContainer container) {
        filterState.addFilter(container.filterKey, container.filter)
    }

    /**
     * Remove the filters for the given widget instance, identified by the filterKey
     * @param filterKey a server generated identifier for a given widget instance
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removefilter")
    void removeFilter(FilterKey filterKey) {
        filterState.removeFilter(filterKey)
    }

    /**
     * Replace the filters for the given filter key. If none exists, this works the same as addFilter(container)
     * @param container An object containing a filter key, a server generated identifier for a given widget instance,
     * and a filter,
     */
    @POST
    @Path("replacefilter")
    @Consumes(MediaType.APPLICATION_JSON)
    void replaceFilter(FilterContainer container) {
        removeFilter(container.filterKey)
        addFilter(container)
    }

    /**
     * Clears all filters.
     */
    @POST
    @Path("clearfilters")
    void clearFilters() {
        filterState.clearAllFilters()
    }
}
