/*
 * Copyright 2015 Next Century Corporation
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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.POST
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

import groovy.json.JsonOutput

/**
 * Service for loading and saving the translation cache.
 */

@Component
@Path("/translationservice")
class TranslationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationService)

    private static final String CACHE_FILENAME = "translation.cache"

    /**
     * Gets the translation cache.
     * @return The map of cached text to translated text
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getcache")
    @SuppressWarnings("JavaIoPackageAccess")
    String getCache() {
        LOGGER.debug("Getting the translation cache...")
        // TODO Use something other than java.io.File to save the translation cache (like a database).
        File cacheFile = new File(CACHE_FILENAME)
        def cache = ["{}"]
        if(cacheFile.exists() && !cacheFile.isDirectory()) {
            cache = cacheFile.collect { it }
        }
        return JsonOutput.toJson(cache[0])
    }

    /**
     * Sets the translation cache.
     * @return true
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("setcache")
    @SuppressWarnings("JavaIoPackageAccess")
    String setCache(String cache) {
        LOGGER.debug("Setting the translation cache...")
        // TODO Use something other than java.io.File to save the translation cache (like a database).
        File cacheFile = new File(CACHE_FILENAME)
        cacheFile.createNewFile()
        cacheFile.text = cache
        return true
    }
}
