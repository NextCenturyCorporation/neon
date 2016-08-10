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

package com.ncc.neon.query.export

import org.codehaus.jackson.annotate.JsonSubTypes
import org.codehaus.jackson.annotate.JsonTypeInfo

/**
 * Marker interface just to give context that implementors are ExportRequests.
 * Also provides JSON metadata to determine which implementation to use.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes([
    @JsonSubTypes.Type(value = ExportQueryRequest, name = 'query')
])
public interface ExportRequest {
}
