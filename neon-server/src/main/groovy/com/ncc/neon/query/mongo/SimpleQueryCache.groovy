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
package com.ncc.neon.query.mongo

import com.ncc.neon.query.result.QueryResult
import org.apache.commons.jcs.engine.control.CompositeCacheManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.commons.jcs.JCS
import org.apache.commons.jcs.access.CacheAccess
import org.apache.commons.jcs.access.exception.CacheException

/**
 * Simple caching service for queries.  When a query is made to Mongo, it is first
 * sent to here to see if it has been cached.  Internally, it turns the MongoQuery into
 * a string and stores it in JavaCachingSystem (JCS).
 *
 * https://commons.apache.org/proper/commons-jcs/index.html
 *
 * Note:   config file is src/main/resources/cache.ccf, which gets
 *         copied to neon/classes/cache.ccf on tomcat
 *
 * TODO:
 *    -- Use Tomcat to manage this rather than simple singleton pattern
 *    -- Manage memory used, or at least figure out how much it is using
 *    -- invalidate cache when data changes
 *    -- re-pull cache when restarted or cache invalidated
 *    -- turn caching on / off dynamically
 *    -- (?) Track cache hits and misses and adjust num objects stored
 */
public class SimpleQueryCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleQueryCache)

    private CacheAccess<String, QueryResult> cache = null

    // Our cache name.
    private final String cacheName = "default"

    // ------------------------------------------------------------
    // Static settings
    // ------------------------------------------------------------
    // time limit for queries, in sec.  If they are fast enough, do not put them in cache, so that
    // the most important queries can be saved.  Currently really high, so cache everything
    static final double QUERY_TIME_LIMIT = 9999999.0

    // How often to print; every X times a query is 'get'.  Want 
    static final int PRINTLIMIT = 20;

    int keyMiss = 0
    int keyHit = 0

    // use internal holder object to hold the instance
    // See: https://www.securecoding.cert.org/confluence/display/java/MSC07-J.+Prevent+multiple+instantiations+of+singleton+objects
    static class SingletonHolder {
        static SimpleQueryCache instance = new SimpleQueryCache()
    }

    public static SimpleQueryCache getSimpleQueryCacheInstance() {
        return SingletonHolder.instance
    }

    private SimpleQueryCache() {
        initializeCache()
    }

    void initializeCache() {
        try {
            cache = JCS.getInstance(cacheName)
        }
        catch (CacheException e) {
            LOGGER.error("Problem initializing cache: ", e.getMessage())
        }
    }

    QueryResult get(MongoQuery mongoQuery) {
        if (!cache) {
            return null
        }
        String queryString = mongoQuery.toString()
        QueryResult result = cache.get(queryString)
        if (result == null) {
            keyMiss++
            LOGGER.debug("Cache miss")
        } else {
            keyHit++
            LOGGER.debug("Cache hit")
        }
        showSummaryStatistics()
        return result
    }

    void put(MongoQuery mongoQuery, QueryResult result) {
        if (!cache) {
            return
        }
        String queryString = mongoQuery.toString()
        try {
            cache.put(queryString, result)
        }
        catch (CacheException e) {
            LOGGER.debug("Problem putting cache: %queryString", e.getMessage())
        }
    }

    void put(MongoQuery mongoQuery, QueryResult result, double queryTime) {
        if (queryTime < QUERY_TIME_LIMIT) {
            return
        }
        put(mongoQuery, result)
    }

    /**
     * Try to figure out what the queries are, what they were doing, how often they hit versus miss
     */
    void showSummaryStatistics() {
        if ((keyMiss + keyHit) % 20 != 0) {
            return
        }
        LOGGER.error(" Cache hits / misses: " + keyHit + " " + keyMiss + " (" + (100 * keyHit / (keyHit + keyMiss)) + "%)")

        def keys = CompositeCacheManager.getInstance().getCache(cacheName).getMemoryCache().getKeySet()
        for (String key : keys) {
            LOGGER.error(" Key:  " + key)
        }
    }
}
