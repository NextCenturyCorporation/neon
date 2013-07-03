package com.ncc.neon.query

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.selection.SelectionManager
import org.slf4j.LoggerFactory

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

/**
 * Executes queries against a data store
 */
abstract class AbstractQueryExecutor implements QueryExecutor {

    private static final def LOGGER = LoggerFactory.getLogger(AbstractQueryExecutor)

    private final SelectionManager selectionManager = new SelectionManager()
    private final FilterState filterState = new FilterState()

    @Override
    UUID addFilter(Filter filter) {
        return filterState.addFilter(filter)
    }

    @Override
    void removeFilter(UUID id) {
        filterState.removeFilter(id)
    }

    @Override
    void clearFilters() {
        filterState.clearFilters()
    }

    @Override
    QueryResult execute(Query query, boolean includedFiltered) {
        return executeQuery(query, null, includedFiltered)
    }

    @Override
    QueryResult execute(QueryGroup query, boolean includeFiltered) {
        def queryGroupResult = new QueryGroupResult()
        query.namedQueries.each {
            def result = execute(it.query, includeFiltered)
            queryGroupResult.namedResults[it.name] = result
        }
        return queryGroupResult
    }

    @Override
    void setSelectionWhere(Filter filter) {
        def res = executeQuery(QueryUtils.queryFromFilter(filter))
        def idField = this.idFieldName
        def ids = res.collect { it.getFieldValue(idField) }
        selectionManager.replaceSelectionWith(ids)
    }

    @Override
    void setSelectedIds(Collection<Object> ids) {
        selectionManager.replaceSelectionWith(transformIdFields(ids))
    }

    @Override
    void addSelectedIds(Collection<Object> ids) {
        selectionManager.addIds(transformIdFields(ids))
    }

    @Override
    void removeSelectedIds(Collection<Object> ids) {
        selectionManager.removeIds(transformIdFields(ids))
    }

    @Override
    void clearSelection() {
        selectionManager.clear()
    }

    @Override
    QueryResult getSelectionWhere(Filter filter) {
        return executeQuery(QueryUtils.queryFromFilter(filter), this.&createWhereClauseForSelectedItems)
    }

    /**
     * Executes the specified query
     * @param query The query to execute
     * @param additionalWhereClauseGenerator A method that generates an additional where clause to apply to the query. This
     * is optional and defaults to null (meaning no additional where clause will be generated)
     * @param includeFiltered If true, global filters are not applied to the query. This is optional and defaults to false.
     * @return
     */
    private QueryResult executeQuery(query, additionalWhereClauseGenerator = null, includeFiltered = false) {
        // construct the query in a way that is specific to this query executor
        def builderResult = buildQuery(query, additionalWhereClauseGenerator, includeFiltered)
        LOGGER.debug("Executing query {}", query)
        return doExecuteQuery(builderResult)
    }

    private def buildQuery(query, additionalWhereClauseGenerator, includeFiltered) {
        def builder = createQueryBuilder()
        applySelectClause(builder, query)
        applyWhereClause(builder, query, additionalWhereClauseGenerator, includeFiltered)

        // we don't currently support distinct and group by together
        if (query.distinctClause) {
            builder.apply(query.distinctClause)
        } else if (query.groupByClauses) {
            applyClauses(builder, query.groupByClauses)
            applyClauses(builder, query.aggregates)
        }
        if (query.sortClauses) {
            applyClauses(builder, query.sortClauses)
        }
        if (query.limitClause) {
            builder.apply(query.limitClause)
        }
        return builder.build()
    }

    protected abstract QueryResult doExecuteQuery(query)

    private def applySelectClause(builder, query) {
        def selectClause = new SelectClause(dataSourceName: query.dataSourceName, datasetId: query.datasetId)
        if (query.fields) {
            selectClause.fields = query.fields
        }
        builder.apply(selectClause)
    }

    private def applyWhereClause(builder, query, additionalWhereClauseGenerator, includeFiltered) {
        def whereClauses = []

        if (!includeFiltered) {
            whereClauses.addAll(createWhereClausesForFilters(query))
        }

        if (additionalWhereClauseGenerator) {
            whereClauses << additionalWhereClauseGenerator()
        }

        if (query.filter.whereClause) {
            whereClauses << query.filter.whereClause
        }
        applyWhereClauses(builder, whereClauses)
    }


    private def applyWhereClauses(builder, whereClauses) {
        if (whereClauses) {
            if (whereClauses.size() == 1) {
                builder.apply(whereClauses[0])
            } else {
                builder.apply(new AndWhereClause(whereClauses: whereClauses))
            }
        }
    }

    private def createWhereClausesForFilters(query) {
        def whereClauses = []
        def filters = filterState.getFiltersForDataset(query.dataSourceName, query.datasetId)
        if (!filters.isEmpty()) {
            filters.each {
                whereClauses << it.whereClause
            }
        }
        return whereClauses
    }

    private def applyClauses(builder, clauses) {
        clauses.each { builder.apply(it) }
    }

    // note this method must be protected (or public) for it to work properly when called from getSelectionWhere
    protected def createWhereClauseForSelectedItems() {
        def selectedIds = selectionManager.selectedIds
        return new SingularWhereClause(lhs: this.idFieldName, operator: 'in', rhs: selectedIds)
    }

    /**
     * Applies a transform to the collection of object ids to allow for
     * transforming the id into a datastore specific format that may be required
     * by that data store driver. The default implementation returns the original
     * collection and performs no transform.
     * @param ids The collection of object ids
     * @return The transformed collection of ids
     */
    protected def transformIdFields(Collection<Object> ids) {
        return ids
    }

    /**
     * Gets the name of the field that indicates the unique id for the record
     * @return
     */
    protected abstract def getIdFieldName()

    /**
     * Creates the builder that executes the query. This will be specific to the implementation to the data store.
     * @return
     */
    protected abstract def createQueryBuilder()

}
