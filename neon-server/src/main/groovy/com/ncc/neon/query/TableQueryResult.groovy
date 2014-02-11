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

package com.ncc.neon.query


/**
 * Query results of a tabular data store.
 * This is represented as a list of rows, where a row is a map of column names to values.
 */

class TableQueryResult implements QueryResult{


    List<Map<String, Object>> data = []

    public TableQueryResult(){

    }

    public TableQueryResult(List<Map<String, Object>> table){
        this.data = table
    }
}


