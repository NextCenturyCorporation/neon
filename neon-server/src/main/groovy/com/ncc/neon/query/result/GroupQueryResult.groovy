/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.query.result

/**
 * Query results of calls to multiple tabular data stores.
 * This is represented as a map, from string IDs to lists of rows, where a row is a map of column names to values.
 */

class GroupQueryResult implements QueryResult {

	final List<List<Map<String, Object>>> data

	public GroupQueryResult() {
		this([])
	}

	public GroupQueryResult(List<List<Map<String, Object>>> table) {
		this.data = table
	}
}