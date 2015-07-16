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

import groovy.transform.ToString

/**
 * Holds information about a field to be exported from a QueryResult.
 */

@ToString(includeNames = true)
class ExportField {
	// The name of the field as it appears in database records, e.g. "created_at".
	String query
	// The "prettified", more human-readable name of the field, e.g. "Date Created".
	String pretty
}