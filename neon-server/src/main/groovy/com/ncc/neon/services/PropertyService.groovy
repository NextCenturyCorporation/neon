/*
 * Copyright 2016 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import org.springframework.stereotype.Component

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.PathParam
import javax.ws.rs.core.MediaType

/**
 * Service for loading and saving properties that are stored in a database. The database may be one
 * of the databases used for Neon queries, or it could be a Derby database.
 */

@Component
@Path("/propertyservice")
class PropertyService {
    private final def properties = [:]

    /**
     * Get the value for a property
     * @param key the property key to look up
     * @return a map, where 'key' is the argument that was passed in, and 'value' is the value of
     * the property, or null if the property does not exist
     */
    @GET
    @Path("{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map getProperty(@PathParam("key") String key) {
        def value = properties.get(key)
        return [key: key, value: value]
    }

    /**
     * Set a property's value
     * @param key the property to set
     * @param value the new value of property
     */
    @POST
    @Path("{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void setProperty(@PathParam("key") String key, String value) {
        properties.put(key, value)
    }

    /**
     * Remove a property. This will succeed whether the key already existed or not.
     * @param key the name of the property to remove
     */
    @DELETE
    @Path("{key}")
    public void remove(@PathParam("key") String key) {
        properties.remove(key)
    }

    /**
     * Get all of the property keys
     * @return a set of all property keys that have values
     */
    @GET
    @Path("*")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> propertyNames() {
        return properties.keySet()
    }
}
