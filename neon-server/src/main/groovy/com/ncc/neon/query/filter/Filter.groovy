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

package com.ncc.neon.query.filter
import com.ncc.neon.query.clauses.WhereClause
import groovy.transform.ToString


/**
 * A filter is applied to a DataSet and can contain a whereClause
 */
@ToString(includeNames = true)
class Filter implements Serializable {

    private static final long serialVersionUID = 7238913369114626126L

    String databaseName
    String tableName

    // Commented by Clark.  According to Intellij, this is never used, so I have commented it out.
    // However, groovy being the way it is, it's hard to tell if it is used indirectly, so I've kept
    // it here as an indication that it might be the solution if a problem crops up.  This is why groovy sucks.
    // String filterName

    WhereClause whereClause

}
