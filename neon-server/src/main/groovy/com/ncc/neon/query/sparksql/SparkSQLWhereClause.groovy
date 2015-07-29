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
import com.ncc.neon.query.clauses.BooleanWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder

/**
 * Utility class for creating Spark SQL WHERE clauses
 */
class SparkSQLWhereClause {

    private WhereClause whereClause

    // used to build up the text of the where clause when toString is called
    private final StringBuilder stringBuilder = new StringBuilder()

    private static final DateTimeFormatter SPARK_SQL_DATETIME_FORMATTER = createSparkSQLDateTimeFormat()

    private static DateTimeFormatter createSparkSQLDateTimeFormat() {
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
        if (clause.operator == 'contains') {
            stringBuilder << formatContains(clause)
        } else if(clause.operator == 'notcontains') {
            stringBuilder << formatNotContains(clause)
        } else {
            stringBuilder << formatLhs(clause.lhs, clause.rhs)
            stringBuilder << " " << formatOperator(clause) << " "
            stringBuilder << formatRhs(clause.rhs)
        }
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

    private static String formatNotContains(clause) {
        return "${clause.lhs} not like '%${clause.rhs}%'"
    }

    private static String formatContains(clause) {
        return "${clause.lhs} like '%${clause.rhs}%'"
    }

    private static String formatLhs(lhs, rhs) {
        return rhs instanceof Date ? "unix_timestamp(${lhs})" : lhs
    }

    private static String formatOperator(clause) {
        def operator = clause.operator
        def value = clause.rhs

        // special cases
        if ( operator == "notin" ) {
            return "not in"
        }
        if ( operator == '=' && !value ) {
            // = null is translated to is null
            return "is"
        }
        if ( operator == '!=' && !value ) {
            // != null is translated to is not null
            return "is not"
        }

        // no special case, operator maps directly to the string
        return operator
    }

    private static String formatRhs(val) {
        if (val instanceof String) {
            return "'${val}'"
        }

        if (val instanceof Collection) {
            return createValuesListString(val)
        }

        if (val instanceof Date) {
            return "unix_timestamp('${return SPARK_SQL_DATETIME_FORMATTER.print(new DateTime(val).withZone(DateTimeZone.UTC))}')"
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
