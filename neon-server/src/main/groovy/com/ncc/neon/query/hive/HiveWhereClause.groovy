/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

package com.ncc.neon.query.hive

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.BooleanWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder

/**
 * Utility class for creating Hive WHERE clauses
 */
class HiveWhereClause {

    private WhereClause whereClause

    // used to build up the text of the where clause when toString is called
    private final StringBuilder stringBuilder = new StringBuilder()

    private static final DateTimeFormatter HIVE_DATETIME_FORMATTER = createHiveDateTimeFormat()

    private static DateTimeFormatter createHiveDateTimeFormat() {
        new DateTimeFormatterBuilder()
                .appendYear(4, 4).appendLiteral('-')
                .appendMonthOfYear(2).appendLiteral('-')
                .appendDayOfMonth(2).appendLiteral(' ')
                .appendHourOfDay(2).appendLiteral(':')
                .appendMinuteOfHour(2).appendLiteral(':')
                .appendSecondOfMinute(2)
                .toFormatter().withZoneUTC()
    }

    @Override
    public String toString() {
        renderClause(whereClause)
        String clause = stringBuilder.toString()
        stringBuilder.length = 0
        return clause
    }

    private String renderClause(SingularWhereClause clause) {
        stringBuilder << formatLhs(clause.lhs, clause.rhs)
        stringBuilder << " " << formatOperator(clause.operator) << " "
        stringBuilder << formatRhs(clause.rhs)
    }

    private String renderClause(BooleanWhereClause clause, String operator) {
        stringBuilder << "("

        clause.whereClauses.eachWithIndex { subClause, index ->
            if (index > 0) {
                stringBuilder << " " << operator << " "
            }
            renderClause(subClause)
        }

        stringBuilder << ")"
    }

    private String renderClause(AndWhereClause clause) {
        renderClause(clause, "and")
    }

    private String renderClause(OrWhereClause clause) {
        renderClause(clause, "or")
    }

    private static String formatLhs(lhs, rhs) {
        return rhs instanceof Date ? "unix_timestamp(${lhs})" : lhs
    }

    private static String formatOperator(operator) {
        // all operators translate directly from standard symbols except for notin
        return operator == "notin" ? "not in" : operator
    }

    private static String formatRhs(val) {
        if (val instanceof String) {
            return "'${val}'"
        }

        if (val instanceof Collection) {
            return createValuesListString(val)
        }

        if (val instanceof Date) {
            return "unix_timestamp('${return HIVE_DATETIME_FORMATTER.print(new DateTime(val).withZone(DateTimeZone.UTC))}')"
        }

        return val
    }

    private static def createValuesListString(collection) {
        def valuesList = []
        collection.each {
            valuesList << formatRhs(it)
        }
        return "(${valuesList.join(',')})"
    }


}
