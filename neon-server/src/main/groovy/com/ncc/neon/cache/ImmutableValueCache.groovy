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
