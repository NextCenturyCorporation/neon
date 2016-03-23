/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.DBObject
import com.mongodb.MongoClient

import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.result.QueryResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AggregateMongoQueryWorker extends AbstractMongoQueryWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMongoQueryWorker)

    AggregateMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        def match = new BasicDBObject('$match', mongoQuery.whereClauseParams)
        List additionalClauses = buildAggregateClauses(mongoQuery)
        if (mongoQuery.query.sortClauses) {
            additionalClauses << new BasicDBObject('$sort', createSortDBObject(mongoQuery.query.sortClauses))
        }
        if (mongoQuery.query.offsetClause) {
            additionalClauses << new BasicDBObject('$skip', mongoQuery.query.offsetClause.offset)
        }
        if (mongoQuery.query.limitClause) {
            additionalClauses << new BasicDBObject('$limit', mongoQuery.query.limitClause.limit)
        }
        LOGGER.debug("Executing aggregate mongo query: {}", [match] + additionalClauses)
        def results = getCollection(mongoQuery).aggregate(match, additionalClauses as DBObject[]).results()
        results = renameNestedGroupByFields(results, mongoQuery.query.groupByClauses)
        return new MongoQueryResult(results)
    }

    private List buildAggregateClauses(mongoQuery) {
        List<String> unwindFields = []
        BasicDBObject groups = new BasicDBObject()
        BasicDBObject projections = new BasicDBObject()

        applyGroupByClauses(mongoQuery, unwindFields, groups, projections)
        applyAggregationClauses(mongoQuery.query.aggregates, groups, projections)

        List<BasicDBObject> unwinds = unwindFields.collect { new BasicDBObject('$unwind', '$' + it) }
        return unwinds + [new BasicDBObject('$group', groups), new BasicDBObject('$project', projections)]
    }

    private void applyGroupByClauses(mongoQuery, unwindFields, groups, projections) {
        BasicDBObject ids = new BasicDBObject()
        groups.put('_id', ids)
        mongoQuery.query.groupByClauses.each { groupByClause ->
            String projectionField
            if (groupByClause instanceof GroupByFieldClause) {
                ids.put(groupByClause.prettyField, '$' + groupByClause.field)
                projectionField = groupByClause.prettyField
                if(mongoQuery.query.aggregateArraysByElement) {
                    MongoUtils.getArrayFields(getCollection(mongoQuery), groupByClause.field).each {
                        if(!unwindFields.contains(it)) {
                            unwindFields << it
                        }
                    }
                }
            } else if (groupByClause instanceof GroupByFunctionClause) {
                ids.put(groupByClause.name, createFunctionDBObject(groupByClause.operation, groupByClause.field))
                // when using a function to compute a field, the resulting field is projected, not the original field
                projectionField = groupByClause.name
                if(mongoQuery.query.aggregateArraysByElement) {
                    MongoUtils.getArrayFields(getCollection(mongoQuery), groupByClause.field).each {
                        if(!unwindFields.contains(it)) {
                            unwindFields << it
                        }
                    }
                }
            } else {
                // this shouldn't happen so make it an error
                throw new Error("Unknown group by clause: type = ${groupByClause.class}, val = ${groupByClause}")
            }
            projections.put(projectionField, '$_id.' + projectionField)
        }
    }

    private void applyAggregationClauses(aggregationClauses, groups, projections) {
        aggregationClauses.each { aggregationClause ->
            groups.put(aggregationClause.name, createFunctionDBObject(aggregationClause.operation, aggregationClause.field))
            // ensure all of the fields from the aggregation operations are shown in the result
            projections.put(aggregationClause.name, 1)
        }
    }

    private def createFunctionDBObject(function, field) {
        def lhs = '$' + function
        def rhs = '$' + field
        // count is implemented as sum with a value of 1
        if (function == 'count') {
            lhs = '$sum'
            rhs = createSumRhs(field)
        }
        return new BasicDBObject(lhs, rhs)
    }

    private def createSumRhs(field) {
        // when a specified field name is specified in the count, the count is the number of rows where that field
        // is not null
        if (field != SelectClause.ALL_FIELDS[0]) {
            return createDBObjectTOCountNonNullValues(field)
        }
        // otherwise just return the raw count
        return 1

    }

    private def createDBObjectTOCountNonNullValues(field) {
        // this method is a little confusing but required for the mongo pipeline, but each step is documented below:

        // compare the field to null (either it's null or doesn't exist). if this returns true, return null,
        // otherwise return the original field value (as long as that value is not null, that's all that matters)
        def ifNull = new BasicDBObject('$ifNull', ['$'+field, null])

        // now compare that previous value (which is either null or the original field value) against null. if
        // it is not equal to null, this condition will return true. if it is null, this condition returns false
        def notNull = new BasicDBObject('$ne', [null, ifNull])

        // if the final value is not null, add 1 to the field, otherwise don't count it
        def conditionalCount = [notNull, 1, 0]
        return new BasicDBObject('$cond', conditionalCount)

    }

    /**
     * Query results with group-by fields in nested objects need to have their pretty field name (e.g. "object->field")
     * changed to their normal field name (e.g. "object.field").
     */
    private BasicDBList renameNestedGroupByFields(results, groupByClauses) {
        def groupByMappings = [:]
        groupByClauses.each { groupByClause ->
            if (groupByClause instanceof GroupByFieldClause && groupByClause.prettyField != groupByClause.field) {
                groupByMappings.put(groupByClause.prettyField, groupByClause.field)
            }
        }

        if(!groupByMappings) {
            return results
        }

        def convertedResults = [] as BasicDBList
        def index = 0

        // TODO How does this loop affect performance?
        results.each { result ->
            convertedResults.add([:] as BasicDBObject)
            result.keySet().each { key ->
                if(key == "_id") {
                    convertedResults[index].put(key, [:] as BasicDBObject)
                    result[key].keySet().each { idKey ->
                        if(groupByMappings[idKey]) {
                            convertedResults[index][key].put(groupByMappings[idKey], result[key][idKey])
                        } else {
                            convertedResults[index][key].put(idKey, result[key][idKey])
                        }
                    }
                } else if(groupByMappings[key]) {
                    convertedResults[index].put(groupByMappings[key], result[key])
                } else {
                    convertedResults[index].put(key, result[key])
                }
            }
            index++
        }

        return convertedResults
    }

}
