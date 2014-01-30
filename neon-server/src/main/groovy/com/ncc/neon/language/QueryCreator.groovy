package com.ncc.neon.language
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.util.DateUtils
import org.apache.commons.lang.math.NumberUtils
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
 */

/**
 * This class listens when a text query is being parsed.
 */

class QueryCreator extends NeonBaseListener {
    private final Map<String, WhereClause> parsedWhereClauses = [:]

    private String collectionName = ""
    private String databaseName = ""
    private WhereClause whereClause
    private LimitClause limitClause
    private OffsetClause offsetClause
    private final List<SortClause> sortClauses = []
    private final List<AggregateClause> aggregates = []
    private final List<GroupByClause> groupByClauses = []
    private final List<String> fields = []

    /**
     * Invoke this method to create a query object after parsing a query with antlr
     * @return a query object
     */

    Query createQuery() {
        Query query = new Query()
        query.fields = fields
        query.filter = new Filter(databaseName: databaseName, tableName: collectionName)
        if (whereClause) {
            query.filter.whereClause = whereClause
        }
        if (limitClause) {
            query.limitClause = limitClause
        }
        if ( offsetClause ) {
            query.offsetClause = offsetClause
        }
        query.sortClauses = sortClauses
        query.aggregates = aggregates
        query.groupByClauses = groupByClauses
        return query
    }

    @Override
    public void exitDatabase(NeonParser.DatabaseContext ctx) {
        databaseName = ctx.STRING()
    }

    @Override
    public void exitQuery(NeonParser.QueryContext ctx) {
        collectionName = ctx.STRING()
    }

    @Override
    void exitSelectFields(NeonParser.SelectFieldsContext ctx) {
        fields.addAll(ctx.text.split(','))
    }

    @Override
    void exitWhereClause(NeonParser.WhereClauseContext ctx) {
        if (ctx.AND()) {
            createBooleanWhereClause(ctx, new AndWhereClause())
        }

        if (ctx.OR()) {
            createBooleanWhereClause(ctx, new OrWhereClause())
        }

        if (parsedWhereClauses.size() == 1) {
            whereClause = parsedWhereClauses.find().value
        }
    }

    @Override
    void exitSimpleWhereClause(NeonParser.SimpleWhereClauseContext ctx) {
        parsedWhereClauses.put(ctx.text, singularWhereClause(ctx))
    }

    private SingularWhereClause singularWhereClause(NeonParser.SimpleWhereClauseContext whereContext) {
        SingularWhereClause singularWhereClause = new SingularWhereClause()

        singularWhereClause.lhs = whereContext.STRING()[0].text
        singularWhereClause.operator = whereContext.operator().text
        def node =  whereContext.WHOLE_NUMBER() ?: whereContext.NUMBER() ?: whereContext.STRING(1)
        singularWhereClause.rhs = handleRhsTypes(node.text)

        return singularWhereClause
    }

    private def handleRhsTypes(String text) {
        if (NumberUtils.isNumber(text)) {
            return Double.valueOf(text)
        }
        if (text == "null") {
            return null
        }
        if (text == '""') {
            return ""
        }
        return DateUtils.tryToParseDate(text)
    }

    private void createBooleanWhereClause(NeonParser.WhereClauseContext ctx, BooleanWhereClause booleanWhereClause) {
        List<WhereClause> clauses = []
        ctx.whereClause().each { NeonParser.WhereClauseContext context ->
            clauses << parsedWhereClauses.remove(escapeContextText(context.text))
        }
        booleanWhereClause.whereClauses = clauses
        parsedWhereClauses.put(escapeContextText(ctx.text), booleanWhereClause)
    }

    private static String escapeContextText(String text) {
        if (text.startsWith("(") && text.endsWith(")")) {
            return text[1..-2]
        }
        return text
    }

    @Override
    public void exitSortClause(NeonParser.SortClauseContext ctx) {
        SortClause sortClause = new SortClause(sortOrder: SortOrder.ASCENDING)
        sortClause.fieldName = ctx.STRING().text
        if (ctx.SORT_DIRECTION()?.text?.toLowerCase() == "desc") {
            sortClause.sortOrder = SortOrder.DESCENDING
        }
        sortClauses << sortClause
    }

    @Override
    void exitGroupClause(NeonParser.GroupClauseContext ctx) {
        if (!ctx.STRING()) {
            // If there is a group by function, ctx.STRING() is null and is handled in exitFunction.
            return
        }

        GroupByFieldClause fieldClause = new GroupByFieldClause()
        fieldClause.field = ctx.STRING().text
        groupByClauses << fieldClause
    }

    @Override
    void exitFunction(NeonParser.FunctionContext ctx) {
        AggregateClause aggregateClause = new AggregateClause()
        // count can take all fields a field name so check for that specially
        aggregateClause.operation = ctx.functionName()?.text ?: ctx.count()?.text
        aggregateClause.field = ctx.STRING()?.text ?: ctx.ALL_FIELDS()?.text
        aggregateClause.name = "${aggregateClause.operation}(${aggregateClause.field})"
        aggregates << aggregateClause
    }

    @Override
    public void exitLimit(NeonParser.LimitContext ctx) {
        LimitClause clause = new LimitClause()
        if(ctx.WHOLE_NUMBER()){
            clause.limit = Integer.valueOf(ctx.WHOLE_NUMBER().text)
        }
        limitClause = clause
    }

    @Override
    void exitOffset(NeonParser.OffsetContext ctx) {
        OffsetClause clause = new OffsetClause()
        if ( ctx.WHOLE_NUMBER())  {
            clause.offset = Integer.valueOf(ctx.WHOLE_NUMBER().text)
        }
        offsetClause = clause
    }
}
