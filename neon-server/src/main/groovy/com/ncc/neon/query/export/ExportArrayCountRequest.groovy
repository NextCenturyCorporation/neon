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

import com.ncc.neon.query.clauses.WhereClause
import groovy.transform.ToString

/**
 * An ExportRequest which contains fields necessary to call QueryService's executeArrayCountQuery method and pull data from the result.
 */

@ToString(includeNames = true)
class ExportArrayCountRequest implements ExportRequest {

	String database
	String table
	String field
	int limit
	String name
	List<ExportField> fields
    WhereClause whereClause
}
