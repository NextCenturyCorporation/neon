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

package com.ncc.neon.query.convert


import java.util.regex.Pattern

/*
 Tests the ElasticSearchConversionStrategy
*/
class ElasticSearchConvertQueryTest extends AbstractConversionTest {
    @Override
    protected def doConvertQuery(query, queryOptions) {
        ElasticSearchConversionStrategy conversionStrategy = new ElasticSearchConversionStrategy(filterState: filterState, selectionState: selectionState)
        conversionStrategy.convertQuery(query, queryOptions)
    }

    @Override
    void assertSimplestConvertQuery(query) {
    }

    @Override
    void assertQueryWithWhereClause(query) {
    }

    @Override
    protected void assertQueryWithSortClause(query) {
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
    }

    @Override
    protected void assertQueryWithOffsetClause(query) {
    }

    @Override
    protected void assertQueryWithDistinctClause(query) {
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
    }

    @Override
    protected void assertQueryWithOrWhereClauseAndFilter(query) {
    }

    @Override
    protected void assertQueryWithWhereNullClause(query) {
    }

    @Override
    protected void assertQueryWithWhereNotNullClause(query) {
    }

    @Override
    protected void assertQueryWithWhereContainsFooClause(query) {
    }

    @Override
    protected void assertQueryWithWhereNotContainsFooClause(query) {
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
    }

    protected BasicDBObject createOrClause() {
    }

    @Override
    protected void assertSelectClausePopulated(query) {
    }

    private void standardQueryAsserts(query) {
    }
}