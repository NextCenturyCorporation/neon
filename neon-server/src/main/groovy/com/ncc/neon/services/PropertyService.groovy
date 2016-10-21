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

/**
 * Service for loading and saving properties that are stored in a database. The database may be one
 * of the databases used for Neon queries, or it could be a Derby database.
 */

class PropertyService {
    private Map<String, String> properties = new HashMap<String, String>()

    public String getProperty(String key) {
        return properties.get(key)
    }

    public void setProperty(String key, String value) {
        properties.put(key, value)
    }

    public void remove(String key) {
        properties.remove(key)
    }

    public Set<String> propertyNames() {
        return properties.keySet()
    }
}
