package com.ncc.neon.query.hive

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.util.DateUtils
import org.junit.Test

/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

class HiveWhereClauseTest {


    @Test
    void "simple where clause with string value"() {
        def lhs = "afield"
        def operator = ">="
        def rhs = "aStringValue"
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} '${rhs}'"
        assertSameClause(expected, new HiveWhereClause(whereClause: whereClause))
    }

    @Test
    void "simple where clause with number value"() {
        def lhs = "afield"
        def operator = "="
        def rhs = 10
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} ${rhs}"
        assertSameClause(expected, new HiveWhereClause(whereClause: whereClause))
    }

    @Test
    void "where in collection"() {
        def lhs = "afield"
        def operator = "in"
        def rhs = ["a", "b", "c"]
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} ${operator} ('a','b','c')"
        assertSameClause(expected, new HiveWhereClause(whereClause: whereClause))
    }

    // not in is tested separately because hive handles not in specially
    @Test
    void "where not in collection"() {
        def lhs = "afield"
        def operator = "notin"
        def rhs = ["a", "b", "c"]
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "${lhs} not in ('a','b','c')"
        assertSameClause(expected, new HiveWhereClause(whereClause: whereClause))
    }

    @Test
    void "where clause with date value"() {
        def lhs = "afield"
        def operator = "<"
        def rhs = DateUtils.tryToParseDate('2013-09-15')
        def whereClause = createSimpleWhereClause(lhs, operator, rhs)
        def expected = "unix_timestamp(${lhs}) ${operator} unix_timestamp('2013-09-15 00:00:00')"
        assertSameClause(expected, new HiveWhereClause(whereClause: whereClause))
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
        assertSameClause(expected, new HiveWhereClause(whereClause: andClause))


    }

    private static SingularWhereClause createSimpleWhereClause(lhs, operator, rhs) {
        return new SingularWhereClause(lhs: lhs, operator: operator, rhs: rhs)
    }

    private static assertSameClause(expected, hiveWhereClause) {
        assert expected.equalsIgnoreCase(hiveWhereClause.toString())
    }

}
