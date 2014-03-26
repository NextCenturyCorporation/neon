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
 * Service for working with a user's current selection.
 */

@Component
@Path("/selectionservice")
class SelectionService {

    /** empty dataset returned when invalid selection is removed or the selection is cleared */
    private static final DataSet EMPTY_DATASET = new DataSet(databaseName: "", tableName: "")

    // Note that selection is just implemented as a series of filters that indicate what is selected

    @Autowired
    SelectionState selectionState

    /**
     * Adds an additional selection
     * @param filterKey The filter that indicates what data is selected
     * @return an ADD selection event
     */
    @POST
    @Path("addselection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SelectionEvent addSelection(FilterKey filterKey) {
        selectionState.addFilter(filterKey)
        return new SelectionEvent(type: "ADD", dataSet: filterKey.dataSet)
    }

    /**
     * Remove the selection
     * @param id The id of the filter that defines the selection to remove
     * @return a REMOVE selection event
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removeselection")
    SelectionEvent removeSelection(String id) {
        FilterKey removed = selectionState.removeFilter(id)
        DataSet dataset = removed ? removed.dataSet : EMPTY_DATASET
        return new SelectionEvent(type: "REMOVE", dataSet: dataset)
    }

    /**
     * Replace the selection. If none exists, this works the same as addSelection(filterKey)
     * @param filterKey The filter that indicates the new selection
     * @return a REPLACE selection event
     */
    @POST
    @Path("replaceselection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SelectionEvent replaceSelection(FilterKey filterKey) {
        removeSelection(filterKey.id)
        addSelection(filterKey)
        return new SelectionEvent(type: "REPLACE", dataSet: filterKey.dataSet)
    }

    /**
     * Clears all selections.
     * @return a CLEAR selection event
     */
    @POST
    @Path("clearselection")
    @Produces(MediaType.APPLICATION_JSON)
    SelectionEvent clearSelection() {
        selectionState.clearAllFilters()
        return new SelectionEvent(type: "CLEAR", dataSet: EMPTY_DATASET)
    }

}
