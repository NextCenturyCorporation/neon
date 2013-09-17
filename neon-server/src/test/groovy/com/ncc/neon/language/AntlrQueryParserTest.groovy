package com.ncc.neon.language
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.SortOrder
import com.ncc.neon.query.filter.Filter
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
 *
 * 
 * @author tbrooks
 */

class AntlrQueryParserTest {

    @Test(expected = NullPointerException)
    void "parsing null is going to throw a NPE"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        parser.parse(null)
    }

    @Test
    void "test simplest query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        Query actual = parser.parse("use db; select * from table")
        Query expected = new Query(filter: new Filter(databaseName: "db", tableName: "table"))

        assert actual.filter.databaseName == expected.filter.databaseName
        assert actual.filter.tableName == expected.filter.tableName
        assert actual.filter.whereClause == expected.filter.whereClause

        assert actual.fields == expected.fields
    }

    @Test
    void "test limit query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        Query actual = parser.parse("use db; select * from table limit 5;")
        assert actual.limitClause.limit == 5
    }

    @Test(expected = NeonParsingException)
    void "test invalid limit query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        parser.parse("use db; select * from table limit 0")
    }

    @Test
    void "test sort then limit query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        Query actual = parser.parse("use db; select * from table sort by field limit 5;")

        assert actual.sortClauses
        assert actual.sortClauses.size() == 1
        assert actual.sortClauses[0].fieldName == "field"
        assert actual.sortClauses[0].sortOrder == SortOrder.ASCENDING
        assert actual.limitClause.limit == 5
    }

    @Test
    void "test limit then sort query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        Query actual = parser.parse("use db; select * from table limit 5 sort by field;")

        assert actual.sortClauses
        assert actual.sortClauses.size() == 1
        assert actual.sortClauses[0].fieldName == "field"
        assert actual.sortClauses[0].sortOrder == SortOrder.ASCENDING
        assert actual.limitClause.limit == 5
    }

    @Test
    void "test group by query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        Query actual = parser.parse("use db; select * from table group by field, sum(field2) sort by field;")

        assert actual.sortClauses
        assert actual.sortClauses.size() == 1
        assert actual.sortClauses[0].fieldName == "field"
        assert actual.sortClauses[0].sortOrder == SortOrder.ASCENDING
        assert actual.groupByClauses
        assert actual.groupByClauses.size() == 1
        assert actual.aggregates
        assert actual.aggregates.size() == 1
    }

    @Test(expected = NeonParsingException)
    void "test syntax error query"() {
        AntlrQueryParser parser = new AntlrQueryParser()
        parser.parse("use db; select from table")
    }

}
