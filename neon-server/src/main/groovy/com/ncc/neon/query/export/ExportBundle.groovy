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
 * A structure for holding ExportRequests, as well as the name of the file those requests should be written to and the type of file
 * they should be written as.
 */

@ToString(includeNames = true)
class ExportBundle {

	String name
	int fileType
	List<ExportRequest> data
}