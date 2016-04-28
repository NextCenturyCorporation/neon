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

import com.ncc.neon.query.mongo.SimpleQueryCache
import com.ncc.neon.query.filter.GlobalFilterState

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Service for running administrator commands.
 */

@Component
@Path("/adminservice")
class AdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService)

    private static final Long SERVER_START_TIME = System.currentTimeMillis()

    /**
     * Clears the mongo query cache.
     * @return true
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/clearquerycache")
    String clearMongoQueryCache() {
        LOGGER.debug("Clearing the mongo query cache...")
        SimpleQueryCache.getSimpleQueryCacheInstance().clear()
        return true
    }

    /**
     * Gets the mongo query cache.
     * @return The map of cached query strings to query result objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getquerycache")
    String displayMongoQueryCache() {
        LOGGER.debug("Getting the mongo query cache...")
        return SimpleQueryCache.getSimpleQueryCacheInstance().getCache()
    }

    /**
     * Gets the statistics for the mongo query cache.
     * @return The list of cached stringified queries and their results
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getquerycachestats")
    String displayMongoQueryCacheStatistics() {
        LOGGER.debug("Getting the mongo query cache statistics...")
        return SimpleQueryCache.getSimpleQueryCacheInstance().generateSummaryStatistics()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getquerycachelimit")
    int displayMongoQueryCacheLimit() {
        LOGGER.debug("Getting the mongo query cache limit...")
        return SimpleQueryCache.getSimpleQueryCacheInstance().getCacheLimit()
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/setquerycachelimit/{cacheLimit}")
    void setMongoQueryCacheLimit(@PathParam("cacheLimit") int cacheLimit) {
        LOGGER.debug("Setting the mongo query cache limit...")
        SimpleQueryCache.getSimpleQueryCacheInstance().setCacheLimit(cacheLimit)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getquerycachetimelimit")
    double displayMongoQueryCacheTimeLimit() {
        LOGGER.debug("Getting the mongo query cache time limit...")
        return SimpleQueryCache.getSimpleQueryCacheInstance().getQueryTimeLimit()
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/setquerycachetimelimit/{cacheTimeLimit}")
    void setMongoQueryCacheTimeLimit(@PathParam("cacheTimeLimit") double cacheTimeLimit) {
        LOGGER.debug("Setting the mongo query cache time limit...")
        SimpleQueryCache.getSimpleQueryCacheInstance().setQueryTimeLimit(cacheTimeLimit)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/iscachingqueries")
    boolean displayMongoCachingQueries() {
        LOGGER.debug("Getting the state of mongo query cache...")
        return SimpleQueryCache.getSimpleQueryCacheInstance().getUsingCache()
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/iscachingqueries/{usingCache}")
    void setMongoCachingQueries(@PathParam("usingCache") boolean usingCache) {
        LOGGER.debug("Setting the state of mongo query cache...")
        SimpleQueryCache.getSimpleQueryCacheInstance().setUsingCache(usingCache)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getmachineinformation")
    Map displayMachineInformation() {
        LOGGER.debug("Getting the machine information...")
        Runtime runtime = Runtime.getRuntime()
        long endTime = System.currentTimeMillis()
        return [
            "osName": System.getProperty("os.name"),
            "osVersion": System.getProperty("os.version"),
            "osArch": System.getProperty("os.arch"),
            "javaVersion": System.getProperty("java.version"),
            "freeMemory": runtime.freeMemory(),
            "totalMemory": runtime.totalMemory(),
            "maxMemory": runtime.maxMemory(),
            "runningTime": (endTime - SERVER_START_TIME)
        ]
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getlogs")
    String displayLogs() {
        LOGGER.debug("Getting the logs...")
        def logDir = "./logs"
        def logDirProperty = "log.dir"
        if(System.getProperty(logDirProperty)) {
            logDir = System.getProperty(logDirProperty)
        }
        InputStream inStream = null
        int data
        String output = "LOGS:\n\n"
        try {
            inStream = new FileInputStream(logDir + "/neon.log")
            data = inStream.read()
            while(data != -1) {
                output += (char) data
                data = inStream.read()
            }
        } finally {
            if(inStream != null) {
                inStream.close()
            }
        }
        return output
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mongo/getsessions")
    int displaySessions() {
        LOGGER.debug("Getting number of active sessions...")
        return GlobalFilterState.activeSessions
    }
}
