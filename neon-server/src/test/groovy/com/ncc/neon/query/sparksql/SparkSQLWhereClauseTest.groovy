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

package com.ncc.neon.query.sparksql
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.util.DateUtils
import org.junit.Test

class SparkSQLWhereClauseTest {


    @Test
    void "simple where clause with string value"() {
        def lhs = "afield"
        def operator = ">="
        def rhs = "aStringValue"
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} '${rhs}'"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: whereClause))
    }

    @Test
    void "simple where clause with number value"() {
        def lhs = "afield"
        def operator = "="
        def rhs = 10
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} ${rhs}"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: whereClause))
    }

    @Test
    void "where in collection"() {
        def lhs = "afield"
        def operator = "in"
        def rhs = ["a", "b", "c"]
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} ('a','b','c')"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: whereClause))
    }

    // "not in" is tested separately because shark handles it specially
    @Test
    void "where not in collection"() {
        def lhs = "afield"
        def operator = "notin"
        def rhs = ["a", "b", "c"]
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} not in ('a','b','c')"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: whereClause))
    }

    @Test
    void "where clause with date value"() {
        def lhs = "afield"
        def operator = "<"
        def rhs = DateUtils.tryToParseDate('2013-09-15')
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "unix_timestamp(${lhs}) ${operator} unix_timestamp('2013-09-15 00:00:00')"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: whereClause))
    }

    @Test
    @SuppressWarnings('MethodSize') // setup is a little long for this test, but pretty straightforward. Explicitly declaring each variable makes it easy to reuse in the expected clause.
    void "nested boolean clause"() {
        def lhs1 = "afield"
        def operator1 = "="
        def rhs1 = 10
        def whereClause1 = createSimpleWhereClause(lhs1, operator1, rhs1)

        def lhs2 = "afield2"
        def operator2 = "!="
        def rhs2 = "aval"
        def whereClause2 = createSimpleWhereClause(lhs2, operator2, rhs2)

        def lhs3 = "afield3"
        def operator3 = ">"
        def rhs3 = "anotherval"
        def whereClause3 = createSimpleWhereClause(lhs3, operator3, rhs3)

        def orClause = new OrWhereClause(whereClauses: [whereClause1, whereClause2])
        def andClause = new AndWhereClause(whereClauses: [orClause, whereClause3])

        def expected = "((${lhs1} ${operator1} ${rhs1} OR ${lhs2} ${operator2} '${rhs2}') AND ${lhs3} ${operator3} '${rhs3}')"
        assertSameClause(expected, new SparkSQLWhereClause(whereClause: andClause))


    }

    private static SingularWhereClause createSimpleWhereClause(lhs, operator, rhs) {
        return new SingularWhereClause(lhs: lhs, operator: operator, rhs: rhs)
    }

    private static assertSameClause(expected, sharkWhereClause) {
        assert expected.equalsIgnoreCase(sharkWhereClause.toString())
    }

}
