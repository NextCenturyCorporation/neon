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

package com.ncc.neon.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap



class ImmutableValueCache<Key,Value> {

    private final ConcurrentMap<Key,Value> cache = [:] as ConcurrentHashMap

    /**
     * Add data to the cache
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key.
     * Values should not be modified by clients.
     * @return the previous value associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with the key,
     *         if the implementation supports null values.)
     *
     **/

    Value add(Key key, Value value){
        return cache.putIfAbsent(key, value)
    }

    Value get(Key key){
        return cache.get(key)
    }

    void remove(Key key){
        cache.remove(key)
    }

    Value replace(Key key, Value value){
        return cache.replace(key, value)
    }



    ConcurrentMap<Key,Value> getCache(){
        return cache
    }
}
