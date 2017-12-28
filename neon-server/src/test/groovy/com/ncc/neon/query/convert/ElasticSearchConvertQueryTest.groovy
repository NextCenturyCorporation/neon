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

import com.ncc.neon.query.elasticsearch.ElasticSearchConversionStrategy
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentParser
import org.junit.After
import org.junit.Assert
import org.junit.Before

/*
 Tests the ElasticSearchConversionStrategy
*/
class ElasticSearchConvertQueryTest extends AbstractConversionTest {
    def builderCallCount

    @Before
    void before() {
        builderCallCount = 0

        super.before()
    }

    @After
    void after() {
        GroovySystem.metaClassRegistry.removeMetaClass(ElasticSearchConversionStrategy)
    }

    @Override
    protected def doConvertQuery(query, queryOptions) {
        ElasticSearchConversionStrategy conversionStrategy = new ElasticSearchConversionStrategy(filterState: filterState, selectionState: selectionState)
        return conversionStrategy.convertQuery(query, queryOptions)
    }

    @Override
    void assertSimplestConvertQuery(query) {
        assert query.indices().length == 1
        assert query.indices()[0] == DATABASE_NAME
        assert query.types().length == 1
        assert query.types()[0] == TABLE_NAME
    }

    @Override
    void assertQueryWithWhereClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        def whereFilter = [:]
        whereFilter.term = [:]
        whereFilter.term[FIELD_NAME] = FIELD_VALUE
        assert map.query.filtered.filter.bool.must.and.filters.contains(whereFilter)
    }

    @Override
    protected void assertQueryWithSortClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        def sortClause = [field: [order: 'asc']]
        assert map.sort.contains(sortClause)
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()
        assert map.size == 5
    }

    @Override
    protected void assertQueryWithOffsetClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map.from == 2
    }

    @Override
    protected void assertQueryWithDistinctClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map.aggregations
        assert map.aggregations == [distinct: [terms: [field:'*', size:0]]]
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map.aggregations
        assert map.aggregations == [_statsFor_field:[stats:[field: FIELD_NAME]]]
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map.aggregations
        assert map.aggregations[FIELD_NAME_2]
        assert map.aggregations[FIELD_NAME_2].terms == [field: FIELD_NAME_2, size: 0]

        assert map.aggregations[FIELD_NAME_2].aggregations
        assert map.aggregations[FIELD_NAME_2].aggregations["${FIELD_NAME}_dayOfWeek"]
    }

    @Override
    protected void assertQueryWithOrWhereClauseAndFilter(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        def subclause1 = [term: [:]]
        subclause1.term[FIELD_NAME] = FIELD_VALUE
        def subclause2 = [term: [:]]
        subclause2.term[FIELD_NAME_2] = FIELD_VALUE
        def clause1 = [bool:[must:[or:[filters:[subclause1, subclause2]]]]]

        def clause2 = [term: [:]]
        clause2.term[FIELD_NAME] = FIELD_VALUE

        map.query.filtered.filter.bool.must.and.filters.contains(clause1)
        map.query.filtered.filter.bool.must.and.filters.contains(clause2)
    }

    @Override
    protected void assertQueryWithWhereNullClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        def whereNullClause
        if (isElasticSearch1()) {
            whereNullClause = [not: [filter: [exists: [field: FIELD_NAME]]]]
        } else if (isElasticSearch2()) {
            whereNullClause = [not: [query: [exists: [field: FIELD_NAME]]]]
        }

        assert map.query.filtered.filter.bool.must.and.filters.contains(whereNullClause)
    }

    @Override
    protected void assertQueryWithWhereNotNullClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        def notNullClause = [exists: [field: FIELD_NAME]]

        assert map.query.filtered.filter.bool.must.and.filters.contains(notNullClause)
    }

    @Override
    protected void assertQueryWithWhereContainsFooClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        if (isElasticSearch1()) {
            def containsClause = [regexp: [:]]
            containsClause.regexp[FIELD_NAME] = '.*foo.*'

            assert map.query.filtered.filter.bool.must.and.filters.contains(containsClause)
        } else if (isElasticSearch2()) {
            assert map.query.filtered.filter.bool.must.and.filters.regexp[FIELD_NAME].value.contains('.*foo.*')
        }
    }

    @Override
    protected void assertQueryWithWhereNotContainsFooClause(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        if (isElasticSearch1()) {
            def containsClause = [not: [filter: [regexp: [:]]]]
            containsClause.not.filter.regexp[FIELD_NAME] = '.*foo.*'

            assert map.query.filtered.filter.bool.must.and.filters.contains(containsClause)
        } else if (isElasticSearch2()) {
            assert map.query.filtered.filter.bool.must.and.filters.not.query.regexp[FIELD_NAME].value.contains('.*foo.*')
        }
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map.query.filtered.filter.bool.must.and.filters == []
        assert map.query.filtered.filter.bool.must.and.filters.size() == 0
    }

    @Override
    protected void assertSelectClausePopulated(query) {
        XContentParser parser = XContentHelper.createParser(query.source())
        def map = parser.map()

        assert map._source.includes.contains(FIELD_NAME)
        assert map._source.includes.contains(FIELD_NAME_2)
    }

    private static boolean isElasticSearch2() {
        return org.elasticsearch.Version.CURRENT.major == 2
    }

    private static boolean isElasticSearch1() {
        return org.elasticsearch.Version.CURRENT.major == 1
    }
}
