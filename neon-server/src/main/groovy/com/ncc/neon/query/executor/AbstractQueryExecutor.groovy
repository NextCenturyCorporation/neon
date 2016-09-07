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

package com.ncc.neon.query.executor

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.result.GroupQueryResult
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.Transform
import com.ncc.neon.query.result.Transformer
import com.ncc.neon.query.result.TransformerNotFoundException
import com.ncc.neon.query.result.TransformerRegistry
import org.springframework.beans.factory.annotation.Autowired
/**
 * Abstract implementation of a QueryExecutor that provides some default functionality useful
 * to all executors
 */
abstract class AbstractQueryExecutor implements QueryExecutor {


    @Autowired
    private SelectionState selectionState

    @Autowired
    TransformerRegistry transformRegistry

    @Override
    QueryResult execute(Query query, QueryOptions options) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }
        QueryResult result = doExecute(query, options)
        return transform(query.transforms, result)
    }

    @Override
    QueryResult execute(QueryGroup queryGroup, QueryOptions options) {
        GroupQueryResult queryResult = new GroupQueryResult()
        Query query
        for(int x = 0; x < queryGroup.queries.size(); x++) {
            query = queryGroup.queries[x]
            if(!isEmptySelection(query, options)) {
                def result = doExecute(query, options)
                result = transform(query.transforms, result)
                queryResult.data << result.data
            }
            else {
                queryResult.data << [data: TabularQueryResult.EMPTY.data]
            }
        }
        return queryResult
    }

    // This method is public due to http://jira.codehaus.org/browse/GROOVY-2433. While this doesn't look
    // like it is being called in a closure, it is exhibiting the same symptoms.

    /**
     * Determines if the query is asking for selection only but there is no selection. In this case,
     * the data returned should be empty
     * @param query
     * @param options
     * @return true if the query will always return an empty result because it is querying on the empty selection
     */
    boolean isEmptySelection(Query query, QueryOptions options) {
        return options.selectionOnly &&
                !selectionState.getFiltersForDataset(new DataSet(databaseName: query.databaseName, tableName: query.tableName))
    }

    protected abstract QueryResult doExecute(Query query, QueryOptions options)

    /**
     * Transforms the query if a transform is provided. If not, the original query result is return unchanged.
     * @param transform
     * @param queryResult
     * @return
     */
    protected QueryResult transform(Transform[] transforms, QueryResult queryResult) {
        if(!transforms || transforms.length == 0){
            return queryResult
        }

        QueryResult returnResult = queryResult
        transforms.each { transform ->
            String transformName = transform.transformName
            Transformer transformer = transformRegistry.getTransformer(transformName)

            if(!transformer){
                throw new TransformerNotFoundException("Transform ${transformName} does not exist.")
            }

            returnResult = transformer.convert(returnResult, transform.params)
        }

        return returnResult
    }
}
