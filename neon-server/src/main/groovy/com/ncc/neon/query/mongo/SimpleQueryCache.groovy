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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.commons.jcs.JCS
import org.apache.commons.jcs.access.CacheAccess
import org.apache.commons.jcs.access.exception.CacheException
import org.apache.commons.jcs.engine.control.CompositeCacheManager
import org.apache.commons.jcs.engine.CompositeCacheAttributes

/**
 * interface for a simple caching service
 */
public class SimpleQueryCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleQueryCache)

        /*
          See:   https://commons.apache.org/proper/commons-jcs/getting_started/intro.html
          for how to use JCS

          jcs.default=
          jcs.default.cacheattributes=org.apache.commons.jcs.engine.CompositeCacheAttributes
          jcs.default.cacheattributes.MaxObjects=1000
          jcs.default.cacheattributes.MemoryCacheName=org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache
        */
    static SimpleQueryCache instance

    static private CacheAccess<MongoQuery, QueryResult> cache = null;

    static synchronized SimpleQueryCache getInstance()
    {
        LOGGER.error("got to getinstance")
        if (instance == null ) { instance = new SimpleQueryCache() } 
        return instance
    }
        
    private SimpleQueryCache() {
        initializeCache()
    }

    void initializeCache()
    {
        if (cache == null) {
            try {

                CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance()
                Properties props = new Properties()
                props.put("jcs.default","DC")
                props.put("jcs.default.cacheattributes", "org.apache.commons.jcs.engine.CompositeCacheAttributes")
                props.put("jcs.default.cacheattributes.MaxObjects","1000")
                props.put("jcs.default.cacheattributes.MemoryCacheName", "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache")

                ccm.configure(props);
                cache = JCS.getInstance("default")
                LOGGER.error("got to here.  cache is now: " + cache)
            }
            catch (CacheException e) {
                LOGGER.error("Problem initializing cache: " + e.getMessage())
            }
        }
    }

    QueryResult get(MongoQuery mongoQuery) {
    
        LOGGER.error("got to get")
        if ( cache == null ) {
            initializeCache()
            if ( cache == null ) {
                LOGGER.error("cache is nulll!!!!")
               return null
            }
        }

        QueryResult result = cache.get(mongoQuery)
            if (result != null) { System.err.println("cache hit") }
            else { System.err.println("cache miss") }
        return result
    }

    void put(MongoQuery mongoQuery, QueryResult result) {
        if ( cache == null ) {
            initializeCache()
            if ( cache == null ) {
                LOGGER.error("cache is nulll!!!!")
               return null
            }
        }

        try {
            cache.put(mongoQuery, result)
                }
        catch (CacheException e) {
            LOGGER.debug("Problem putting cache: %s", e.getMessage())
        }
    }

}
