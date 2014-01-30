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

package com.ncc.neon.language
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.SortOrder
import com.ncc.neon.query.filter.Filter
import org.junit.Before
import org.junit.Test


class AntlrQueryParserTest {

    private AntlrQueryParser parser

    @Before
    void before(){
        parser = new AntlrQueryParser()
    }

    @Test(expected = NeonParsingException)
    void "syntax error query"() {
        //No select fields present so its an error.
        parser.parse("use db; select from table")
    }

    @Test
    void "simplest query"() {
        Query actual = parser.parse("use db; select * from table")
        Query expected = new Query(filter: new Filter(databaseName: "db", tableName: "table"))

        assert actual.filter.databaseName == expected.filter.databaseName
        assert actual.filter.tableName == expected.filter.tableName
        assert actual.filter.whereClause == expected.filter.whereClause

        assert actual.fields == expected.fields
    }

    @Test
    void "where clause query"() {
        Query actual = parser.parse("use db; select * from table where field = 5")
        SingularWhereClause whereClause = new SingularWhereClause(lhs: "field", operator: "=", rhs: 5)
        Query expected = new Query(filter: new Filter(databaseName: "db", tableName: "table", whereClause: whereClause))

        assert actual.filter.databaseName == expected.filter.databaseName
        assert actual.filter.tableName == expected.filter.tableName
        assert actual.filter.whereClause.lhs == expected.filter.whereClause.lhs
        assert actual.filter.whereClause.rhs == expected.filter.whereClause.rhs
        assert actual.filter.whereClause.operator == expected.filter.whereClause.operator

        assert actual.fields == expected.fields
    }

    @Test
    void "limit query"() {
        Query actual = parser.parse("use db; select * from table limit 5;")
        assert actual.limitClause.limit == 5
    }

    @Test(expected = NeonParsingException)
    void "test invalid limit query"() {
        parser.parse("use db; select * from table limit 0")
    }

    @Test
    void "sort then limit query"() {
        Query actual = parser.parse("use db; select * from table sort by field limit 5;")

        assertSortClauseSetProperly(actual)
        assert actual.limitClause.limit == 5
    }

    @Test
    void "limit then sort query"() {
        Query actual = parser.parse("use db; select * from table limit 5 sort by field;")

        assertSortClauseSetProperly(actual)
        assert actual.limitClause.limit == 5
    }

    @Test
    void "group by and sort by query"() {
        Query actual = parser.parse("use db; select * from table group by field, sum(field2) sort by field;")

        assertSortClauseSetProperly(actual)
        assert actual.groupByClauses
        assert actual.groupByClauses.size() == 1
        assert actual.groupByClauses[0].field == "field"

        assert actual.aggregates
        assert actual.aggregates.size() == 1
        assert actual.aggregates[0].name == "sum(field2)"
        assert actual.aggregates[0].operation == "sum"
        assert actual.aggregates[0].field == "field2"
    }

    @Test
    void "count all fields"() {
        Query actual = parser.parse("use db; select * from table group by field, count(*) sort by field;")

        assertSortClauseSetProperly(actual)
        assert actual.groupByClauses
        assert actual.groupByClauses.size() == 1
        assert actual.groupByClauses[0].field == "field"

        assert actual.aggregates
        assert actual.aggregates.size() == 1
        assert actual.aggregates[0].name == "count(*)"
        assert actual.aggregates[0].operation == "count"
        assert actual.aggregates[0].field == "*"
    }

    @Test
    void "count named field"() {
        Query actual = parser.parse("use db; select * from table group by field, count(field2) sort by field;")

        assertSortClauseSetProperly(actual)
        assert actual.groupByClauses
        assert actual.groupByClauses.size() == 1
        assert actual.groupByClauses[0].field == "field"

        assert actual.aggregates
        assert actual.aggregates.size() == 1
        assert actual.aggregates[0].name == "count(field2)"
        assert actual.aggregates[0].operation == "count"
        assert actual.aggregates[0].field == "field2"
    }

    private void assertSortClauseSetProperly(Query actual) {
        assert actual.sortClauses
        assert actual.sortClauses.size() == 1
        assert actual.sortClauses[0].fieldName == "field"
        assert actual.sortClauses[0].sortOrder == SortOrder.ASCENDING
    }

}
