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

package com.ncc.neon.query.clauses

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo



/**
 * Marker interface just to give context that implementors are WhereClauses.
 * Also provides JSON metadata to determine which implementation to use
 */


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes([
    @JsonSubTypes.Type(value = SingularWhereClause, name = 'where'),
    @JsonSubTypes.Type(value = WithinDistanceClause, name =  'withinDistance'),
    @JsonSubTypes.Type(value = GeoIntersectionClause, name =  'geoIntersection'),
    @JsonSubTypes.Type(value = GeoWithinClause, name =  'geoWithin'),
    @JsonSubTypes.Type(value = AndWhereClause, name =  'and'),
    @JsonSubTypes.Type(value = OrWhereClause, name =  'or')
])
public interface WhereClause {
}
