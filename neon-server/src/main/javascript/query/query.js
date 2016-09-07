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

/**
 * Represents a query to be constructed against some data source. This class is built so query
 * clauses can be chained together to create an entire query.
 * @example
 *     var where = neon.query.where;
 *     var and = neon.query.and;
 *     var query = new neon.query.Query(where(and(where('someProperty','=',5), where('someOtherProperty','<',10))));
 * @class neon.query.Query
 * @constructor
 */
neon.query.Query = function() {
    this.filter = new neon.query.Filter();
    this.fields = ['*'];
    this.aggregateArraysByElement = false;
    this.ignoreFilters_ = false;
    this.selectionOnly_ = false;

    // use this to ignore specific filters
    this.ignoredFilterIds_ = [];

    this.groupByClauses = [];
    this.isDistinct = false;
    this.aggregates = [];
    this.sortClauses = [];
    this.limitClause = undefined;
    this.transforms = undefined;
};

/**
 * Produce a human-readable representation of the query
 * @method toString
 * @return {string}
 */
neon.query.Query.prototype.toString = function() {
    var s = " ";
    this.fields.forEach(function(d) {
        s = s + " SELECT " + d;
    });
    this.sortClauses.forEach(function(d) {
        s = s + ", SORTBY " + d.fieldName + " (" + d.sortOrder + ")";
    });
    this.groupByClauses.forEach(function(d) {
        s = s + ", GROUPBY " + d.field;
    });
    this.aggregates.forEach(function(d) {
        s = s + ", AGGREGATE " + d.operation +  " ON " + d.field + " (named " + d.name + ")";
    });
    if(this.limitClause !== undefined) {
        s = s + ", LIMIT " + this.limitClause.limit;
    }
    return s;
};

/**
 * The aggregation operation to count items
 * @property COUNT
 * @type {String}
 */
neon.query.COUNT = 'count';

/**
 * The aggregation operation to sum items
 * @property SUM
 * @type {String}
 */
neon.query.SUM = 'sum';

/**
 * The aggregation operation to get the maximum value
 * @property MAX
 * @type {String}
 */
neon.query.MAX = 'max';

/**
 * The aggregation operation to get the minimum value
 * @property MIN
 * @type {String}
 */
neon.query.MIN = 'min';

/**
 * The aggregation operation to get the average value
 * @property AVG
 * @type {String}
 */
neon.query.AVG = 'avg';

/**
 * The sort parameter for clauses to sort ascending
 * @property ASCENDING
 * @type {int}
 */
neon.query.ASCENDING = 1;

/**
 * The sort parameter for clauses to sort descending
 * @property DESCENDING
 * @type {int}
 */
neon.query.DESCENDING = -1;

/**
 * The function name to get the month part of a date field
 * @property MONTH
 * @type {String}
 */
neon.query.MONTH = 'month';

/**
 * The function name to get the day part of a date field
 * @property DAY
 * @type {String}
 */
neon.query.DAY = 'dayOfMonth';

/**
 * The function name to get the year part of a date field
 * @property YEAR
 * @type {String}
 */
neon.query.YEAR = 'year';

/**
 * The function name to get the hour part of a date field
 * @property HOUR
 * @type {String}
 */
neon.query.HOUR = 'hour';

/**
 * The function name to get the minute part of a date field
 * @property MINUTE
 * @type {String}
 */
neon.query.MINUTE = 'minute';

/**
 * The function name to get the second part of a date field
 * @property SECOND
 * @type {String}
 */
neon.query.SECOND = 'second';

/**
 * The distance unit for geospatial queries in meters
 * @property METER
 * @type {String}
 */
neon.query.METER = 'meter';

/**
 * The distance unit for geospatial queries in kilometers
 * @property KM
 * @type {String}
 */
neon.query.KM = 'km';

/**
 * The distance unit for geospatial queries in miles
 * @property MILE
 * @type {String}
 */
neon.query.MILE = 'mile';

/**
 * Sets the *select* clause of the query to select data from the specified table
 * @method selectFrom
 * @param {String} databaseName
 * @param {String} tableName table to select from
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.selectFrom = function(databaseName, tableName) {
    this.filter.selectFrom(databaseName, tableName);
    return this;
};

/**
 * Sets the fields that should be included in the result. If not specified,
 * all fields will be included (equivalent to SELECT *).
 * @method withFields
 * @param {...String | Array} fields A variable number of strings or single Array of Strings indicating which fields should be included
 * @return {neon.query.Query} This query object
 * @example
 *     new neon.query.Query(...).withFields("field1","field2");
 */
neon.query.Query.prototype.withFields = function(fields) {
    if(arguments.length === 1 && $.isArray(fields)) {
        this.fields = fields;
    } else {
        this.fields = neon.util.arrayUtils.argumentsToArray(arguments);
    }
    return this;
};

/**
 * Sets the *where* clause of the query to determine how to select the data
 * The arguments can be either<br>
 * 3 arguments as follows:
 *  <ul>
 *      <li>arguments[0] - The property to filter on in the database</li>
 *      <li>arguments[1] - The filter operator</li>
 *      <li>arguments[2] - The value to filter against</li>
 *  </ul>
 * OR <br>
 * A boolean operator (neon.Query.and or neon.Query.or)
 * </ol>
 * @example
 *     where('someProperty','=',5)
 *
 *     where(neon.Query.and(where('someProperty','=',5), where('someOtherProperty','<',10)))
 * @method where
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.where = function() {
    this.filter.where.apply(this.filter, arguments);
    return this;
};

/**
 * Groups the results by the specified field(s)
 * @method groupBy
 * @param {...String|...neon.query.GroupByFunctionClause|Array} fields One or more fields to group the results by.
 * Each parameter can be a single field name, a {{#crossLink "neon.query.GroupByFunctionClause"}}{{/crossLink}}. Alternatively a
 * single array containing field names and/or GroupByFunctionClause objects.
 * @return {neon.query.Query} This query object
 * @example
 *    var averageAmount = new neon.query.GroupByFunctionClause(neon.query.AVG, 'amount', 'avg_amount');
 *    new neon.query.Query(...).groupBy('field1',averageAmount);
 */
neon.query.Query.prototype.groupBy = function(fields) {
    // even though internally each groupBy clause is a separate object (since single field and functions
    // are processed differently), the user will think about a single groupBy operation which may include
    // multiple fields, so this method does not append to the existing groupBy fields, but replaces them
    this.groupByClauses.length = 0;
    var me = this;

    var list;
    if(arguments.length === 1 && $.isArray(fields)) {
        list = fields;
    } else {
        list = neon.util.arrayUtils.argumentsToArray(arguments);
    }

    list.forEach(function(field) {
        var clause = field;

        // if the user provided a string or object with a columnName and prettyName, convert that to the groupBy
        // representation of a single field, otherwise, they provided a groupBy function so just use that
        if(typeof field === 'string') {
            clause = new neon.query.GroupBySingleFieldClause(field, field);
        } else if(typeof field === 'object' && !(field instanceof neon.query.GroupByFunctionClause)) {
            clause = new neon.query.GroupBySingleFieldClause(field.columnName, field.prettyName);
        }
        me.groupByClauses.push(clause);
    });
    return this;
};

/**
 * Creates a new field with the specified name that aggregates the field with the given operation
 * @param {String} aggregationOperation The operation to aggregate by. See the constants in this
 * class for operators (e.g. SUM, COUNT). This function may be called multiple times to include
 * multiple aggregation fields.
 * @param {String} aggregationField The field to perform the aggregation on
 * @param {String} [name] The name of the new field generated by this operation. If not specified, a name
 * will be generated based on the operation name and the field name
 * @method aggregate
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.aggregate = function(aggregationOperation, aggregationField, name) {
    var newFieldName = name != null ? name : (aggregationOperation + '(' + aggregationField + ')');
    this.aggregates.push(new neon.query.FieldFunction(aggregationOperation, aggregationField, newFieldName));
    return this;
};

/**
 * Specifies the query return distinct results
 * @method distinct
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.distinct = function() {
    this.isDistinct = true;
    return this;
};

/**
 * Specifies a limit on the maximum number of results that should be returned from the query
 * @method limit
 * @param {int} limit The maximum number of results to return from the query
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.limit = function(limit) {
    this.limitClause = new neon.query.LimitClause(limit);
    return this;
};

/**
 * Specifies an offset to start the results from. This can be used in combination with limit for pagination
 * @method offset
 * @param {int} offset The number of rows to skip in the results
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.offset = function(offset) {
    this.offsetClause = new neon.query.OffsetClause(offset);
    return this;
};

/**
 *
 * Configures the query results to be sorted by the specified field(s). To sort by multiple fields, repeat the
 * 2 parameters multiple times
 * @method sortBy
 * @param {String | Array} fieldName The name of the field to sort on OR single array in the form
 * of ['field1', neon.query.ASC, ... , 'fieldN', neon.query.DESC]
 * @param {int} sortOrder The sort order (see the constants in this class)
 * @return {neon.query.Query} This query object
 * @example
 *     new neon.query.Query(...).sortBy('field1',neon.query.ASC,'field2',neon.query.DESC);
 */
neon.query.Query.prototype.sortBy = function(fields) {
    // even though internally each sortBy clause is a separate object, the user will think about a single sortBy
    // operation which may include multiple fields, so this method does not append to the existing
    // sortBy fields, but replaces them
    this.sortClauses.length = 0;

    var list;
    if(arguments.length === 1 && $.isArray(fields)) {
        list = fields;
    } else {
        list = neon.util.arrayUtils.argumentsToArray(arguments);
    }

    for(var i = 1; i < list.length; i += 2) {
        var field = list[i - 1];
        var order = list[i];
        this.sortClauses.push(new neon.query.SortClause(field, order));
    }
    return this;
};

/**
 * Adds a transform to this query
 * @method transform
 * @param {neon.query.Transform} transformObj a transform to be applied to the data
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.transform = function(transformObj) {
    var transforms;
    if(arguments.length === 1 && $.isArray(transformObj)) {
        transforms = transformObj;
    } else {
        transforms = neon.util.arrayUtils.argumentsToArray(arguments);
    }

    this.transforms = transforms;
    return this;
};

/**
 * Sets the query to ignore any filters that are currently applied
 * @method ignoreFilters
 * @param {...String | Array} [filterIds] An optional, variable number of filter ids to ignore OR an array of
 * filter ids to ignore. If specified, only these filters will be ignored. Otherwise, all will be ignored.
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.ignoreFilters = function(filterIds) {
    var filters;
    if(arguments.length === 1 && $.isArray(filterIds)) {
        filters = filterIds;
    } else {
        filters = neon.util.arrayUtils.argumentsToArray(arguments);
    }

    if(filters.length > 0) {
        this.ignoredFilterIds_ = filters;
    } else {
        this.ignoreFilters_ = true;
    }
    return this;
};

/**
 * Sets the query to return just the current selection
 * @method selectionOnly
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.selectionOnly = function() {
    this.selectionOnly_ = true;
    return this;
};

neon.query.Query.prototype.enableAggregateArraysByElement = function() {
    this.aggregateArraysByElement = true;
    return this;
};

neon.query.Query.prototype.geoIntersection = function(locationField, points, geometryType) {
    this.filter.geoIntersection(locationField, points, geometryType);
    return this;
};

neon.query.Query.prototype.geoWithin = function(locationField, points) {
    this.filter.geoWithin(locationField, points);
    return this;
};

/**
 * Adds a query clause to specify that query results must be within the specified distance from
 * the center point. This is used instead of a *where* query clause (or in conjuction with where
 * clauses in boolean operators).
 * @method withinDistance
 * @param {String} locationField The name of the field containing the location value
 * @param {neon.util.LatLon} center The point from which the distance is measured
 * @param {double} distance The maximum distance from the center point a result must be within to be returned in the query
 * @param {String} distanceUnit The unit of measure for the distance. See the constants in this class.
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.withinDistance = function(locationField, center, distance, distanceUnit) {
    this.filter.withinDistance(locationField, center, distance, distanceUnit);
    return this;
};

/**
 * Utility methods for working with Queries
 * @class neon.query
 * @static
 */

/**
 * Creates a simple *where* clause for use with filters or queries
 * @method where
 * @param {String} fieldName The field name to group on
 * @param {String} op The operation to perform
 * @param {Object}  value The value to compare the field values against
 * @example
 *     where('x','=',10)
 * @return {Object}
 */
neon.query.where = function(fieldName, op, value) {
    return new neon.query.WhereClause(fieldName, op, value);
};

/**
 * Creates an *and* boolean clause for the query
 * @method and
 * @param  {Object | Array} clauses A variable number of *where* clauses to apply OR an single array of *where* clauses
 * @example
 *     and(where('x','=',10),where('y','=',1))
 * @return {Object}
 */
neon.query.and = function(clauses) {
    if(arguments.length === 1 && $.isArray(clauses)) {
        return new neon.query.BooleanClause('and', clauses);
    } else {
        return new neon.query.BooleanClause('and', neon.util.arrayUtils.argumentsToArray(arguments));
    }
};

/**
 * Creates an *or* boolean clause for the query
 * @method or
 * @param {Object} clauses A variable number of *where* clauses to apply OR a single array of *where* clauses
 * @example
 *     or(where('x','=',10),where('y','=',1))
 * @return {Object}
 */
neon.query.or = function(clauses) {
    if(arguments.length === 1 && $.isArray(clauses)) {
        return new neon.query.BooleanClause('or', clauses);
    } else {
        return new neon.query.BooleanClause('or', neon.util.arrayUtils.argumentsToArray(arguments));
    }
};

/**
 * Creates a query clause to specify that query results must be within the specified distance from
 * the center point.
 * @method withinDistance
 * @param {String} locationField The name of the field containing the location value
 * @param {neon.util.LatLon} center The point from which the distance is measured
 * @param {double} distance The maximum distance from the center point a result must be within to be returned in the query
 * @param {String} distanceUnit The unit of measure for the distance. See the constants in this class.
 * @return {neon.query.WithinDistanceClause}
 */
neon.query.withinDistance = function(locationField, center, distance, distanceUnit) {
    return new neon.query.WithinDistanceClause(locationField, center, distance, distanceUnit);
};

/**
 * A generic function that can be applied to a field (on the server side). For example, this could be an aggregation
 * function such as an average or it could be a function to manipulate a field value such as extracting the month
 * part of a date
 * @param {String} operation The name of the operation to perform
 * @param {String} field The name of the field to perform the operation on
 * @param {String} name The name of the field created by performing this operation
 * @class neon.query.FieldFunction
 * @constructor
 * @private
 */
neon.query.FieldFunction = function(operation, field, name) {
    this.operation = operation;
    this.field = field;
    this.name = name;
};

/**
 * A function for deriving a new field to use as a group by. For example, a month field might be
 * generated from a date
 * @param {String} operation The name of the operation to perform
 * @param {String} field The name of the field to perform the operation on
 * @param {String} name The name of the field created by performing this operation
 * @class neon.query.GroupByFunctionClause
 * @constructor
 */
neon.query.GroupByFunctionClause = function(operation, field, name) {
    this.type = 'function';
    neon.query.FieldFunction.call(this, operation, field, name);
};
// TODO: NEON-73 (Javascript inheritance library)
neon.query.GroupByFunctionClause.prototype = new neon.query.FieldFunction();

// These are not meant to be instantiated directly but rather by helper methods
neon.query.GroupBySingleFieldClause = function(field, prettyField) {
    this.type = 'single';
    this.field = field;
    this.prettyField = ((prettyField.indexOf(".") >= 0) ? prettyField.replace(/\./g, "->") : prettyField);
};

neon.query.BooleanClause = function(type, whereClauses) {
    this.type = type;
    this.whereClauses = whereClauses;
};

neon.query.WhereClause = function(lhs, operator, rhs) {
    this.type = 'where';
    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;
};

neon.query.SortClause = function(fieldName, sortOrder) {
    this.fieldName = fieldName;
    this.sortOrder = sortOrder;
};

neon.query.LimitClause = function(limit) {
    this.limit = limit;
};

neon.query.OffsetClause = function(offset) {
    this.offset = offset;
};

neon.query.WithinDistanceClause = function(locationField, center, distance, distanceUnit) {
    this.type = 'withinDistance';
    this.locationField = locationField;
    this.center = center;
    this.distance = distance;
    this.distanceUnit = distanceUnit;
};

neon.query.intersectionClause = function(locationField, points, geometryType) {
    this.type = "geoIntersection";
    this.locationField = locationField;
    this.points = points;
    this.geometryType = geometryType;
};

neon.query.withinClause = function(locationField, points) {
    this.type = "geoIntersection";
    this.locationField = locationField;
    this.points = points;
};
