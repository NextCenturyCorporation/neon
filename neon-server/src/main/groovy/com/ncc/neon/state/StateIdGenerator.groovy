/*
 * Copyright 2014 Next Century Corporation
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

package com.ncc.neon.state

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Generates unique ids for widgets to use for saving and restoring state
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class StateIdGenerator implements Serializable {

    private static final long serialVersionUID = - 8385782026473536008L

    /** stores a mapping of text qualifiers to the id associated with that text */
    private final ConcurrentMap<String, UUID> map = new ConcurrentHashMap<String, UUID>()

    /**
     * Gets a unique id for a widget to use. This id will be consistent throughout the session
     * @return
     */
    UUID getId() {
        return getId("")
    }

    /**
     * Gets an id associated with the specified qualifier text. This is provided to allow the generation of
     * multiple ids for a single session.
     * @param qualifier A text qualifier that is associated with the generated id. Each time an id for
     * this string is requested, it will be the same during the session.
     * @return
     */
    UUID getId(String qualifier) {
        map.putIfAbsent(qualifier, UUID.randomUUID())
        return map.get(qualifier)
    }

}
