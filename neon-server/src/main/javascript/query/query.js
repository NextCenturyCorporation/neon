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
 * The url of the query server. Defaults to localhost:8080/neon.
 * @property SERVER_URL
 * @type {String}
 */
neon.query.SERVER_URL = 'http://localhost:8080/neon';

/**
 * Represents a query to be constructed against some data source. This class is built so query
 * clauses can be chained together to create an entire query, as shown below
 * @example
 *     var where = neon.query.where;
 *     var and = neon.query.and;
 *     var query = new neon.query.Query(where(and(where('someProperty','=',5), where('someOtherProperty','<',10))));
 *     neon.query.executeQuery(query);
 * @namespace neon.query
 * @class Query
 * @constructor
 */
neon.query.Query = function () {


    this.filter = new neon.query.Filter();
    this.fields = ['*'];

    /*jshint expr: true */
    this.includeFiltered_ = false;

    this.groupByClauses = [];
    this.distinctClause;
    this.aggregates = [];
    this.sortClauses = [];
    this.limitClause;

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
 * @property MAX
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


// these ids are used for providing json args to the callback functions
neon.query.TABLE_NAME_IDENTIFIER = 'tableName';
neon.query.DATABASE_NAME_IDENTIFIER = 'databaseName';

/**
 * Sets the *select* clause of the query to select data from the specified dataset
 * @method selectFrom
 * @param {String} databaseName The name of the database that contains the data
 * @param {String} tableName The dataset to select from
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.selectFrom = function (databaseName, tableName) {
    this.filter.selectFrom.apply(this.filter, arguments);
    return this;
};

/**
 * Sets the fields that should be included in the result. If not specified,
 * all fields will be included (equivalent to SELECT *).
 * @method withFields
 * @param {...String} fields A variable number of strings indicating which fields should be included
 * @return {neon.query.Query} This query object
 * @example
 *     new neon.query.Query(...).withFields("field1","field2");
 */
neon.query.Query.prototype.withFields = function (fields) {
    this.fields = neon.util.ArrayUtils.argumentsToArray(arguments);
    return this;
};

/**
 * Sets the *where* clause of the query to determine how to select the data
 * @method where
 * @param {Object} arguments The arguments can be in either multiple formats<br>
 * <ol>
 *    <li>A 3 argument array as follows:
 *      <ul>
 *          <li>arguments[0] - The property to filter on in the database</li>
 *          <li>arguments[1] - The filter operator</li>
 *          <li>arguments[2] - The value to filter against</li>
 *      </ul>
 *    </li>
 *    <li>A boolean operator (and/or)</li>
 * </ol>
 * @example
 *     where('someProperty','=',5)
 *
 *     where(neon.Query.and(where('someProperty','=',5), where('someOtherProperty','<',10)))
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.where = function () {
    this.filter.where.apply(this.filter, arguments);
    return this;
};

/**
 * Groups the results by the specified field(s)
 * @method groupBy
 * @param {...String|...neon.query.GroupByFunctionClause} fields One or more fields to group the results by.
 * Each parameter can be a single field name or a {{#crossLink "neon.query.GroupByFunctionClause"}}{{/crossLink}}
 * @return {neon.query.Query} This query object
 * @example
 *    var averageAmount = new neon.query.GroupByFunctionClause(neon.query.AVG, 'amount', 'avg_amount');
 *    new neon.query.Query(...).groupBy('field1',averageAmount);
 */
neon.query.Query.prototype.groupBy = function (fields) {
    // even though internally each groupBy clause is a separate object (since single field and functions
    // are processed differently), the user will think about a single groupBy operation which may include
    // multiple fields, so this method does not append to the existing groupBy fields, but replaces them
    this.groupByClauses.length = 0;
    var me = this;

    var list = neon.util.ArrayUtils.argumentsToArray(arguments);
    list.forEach(function (field) {
        // if the user provided a string, convert that to the groupBy representation of a single field, otherwise,
        // they provided a groupBy function so just use that
        var clause = typeof field === 'string' ? new neon.query.GroupBySingleFieldClause(field) : field;
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
 * @param {String} name The name of the new field generated by this operation
 * @method aggregate
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.aggregate = function (aggregationOperation, aggregationField, name) {
    this.aggregates.push(new neon.query.FieldFunction(aggregationOperation, aggregationField, name));
    return this;
};

/**
 * Specifies a DISTINCT clause that returns only the distinct values of the specified field
 * @method distinct
 * @param {String} fieldName The name of the field to return distinct values for
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.distinct = function (fieldName) {
    this.distinctClause = new neon.query.DistinctClause(fieldName);
    return this;
};

/**
 * Specifies a limit on the maximum number of results that should be returned from the query
 * @method limit
 * @param {int} limit The maximum number of results to return from the query
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.limit = function (limit) {
    this.limitClause = new neon.query.LimitClause(limit);
    return this;
};

/**
 *
 * Configures the query results to be sorted by the specified field(s). To sort by multiple fields, repeat the
 * 2 parameters multiple times
 * @method sortBy
 * @param {String} fieldName The name of the field to sort on
 * @param {int} sortOrder The sort order (see the constants in this class)
 * @return {neon.query.Query} This query object
 * @example
 *     new neon.query.Query(...).sortBy('field1',neon.query.ASC,'field2',neon.query.DESC);
 */
neon.query.Query.prototype.sortBy = function (fieldName, sortOrder) {
    // even though internally each sortBy clause is a separate object, the user will think about a single sortBy
    // operation which may include multiple fields, so this method does not append to the existing
    // sortBy fields, but replaces them
    this.sortClauses.length = 0;
    var list = neon.util.ArrayUtils.argumentsToArray(arguments);
    for (var i = 0; i < list.length; i += 2) {
        var field = list[i];
        var order = list[i + 1];
        this.sortClauses.push(new neon.query.SortClause(field, order));
    }
    return this;
};


/**
 * Indicates whether or not even data outside of the current filters should be returned
 * @method includeFiltered
 * @param {Boolean} includeFiltered true to include data outside of the current filters, false to just return
 * the data matched by the current filters (defaults to false)
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.includeFiltered = function (includeFiltered) {
    this.includeFiltered_ = includeFiltered;
    return this;
};

/**
 * Specifies a name of a transform to apply to the json before returning it from the query
 * @method transform
 * @param {String} transformName
 * @param {Array} [transformParams] If the transform takes any parameters, pass them in here
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.transform = function (transformName, transformParams) {
    this.filter.transform(transformName, transformParams);
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
neon.query.Query.prototype.withinDistance = function (locationField, center, distance, distanceUnit) {
    this.filter.withinDistance(locationField, center, distance, distanceUnit);
    return this;
};

/**
 * Creates a simple *where* clause for the query
 * @method where
 * @param {String} fieldName The field name to group on
 * @param {String} op The operation to perform
 * @param {Object}  value The value to compare the field values against
 * @example
 *     where('x','=',10)
 * @return {Object}
 */
neon.query.where = function (fieldName, op, value) {
    return new neon.query.WhereClause(fieldName, op, value);
};

/**
 * Creates an *and* boolean clause for the query
 * @method and
 * @param  {Object} clauses A variable number of *where* clauses to apply
 * @example
 *     and(where('x','=',10),where('y','=',1))
 * @return {Object}
 */
neon.query.and = function (clauses) {
    return new neon.query.BooleanClause('and', neon.util.ArrayUtils.argumentsToArray(arguments));
};

/**
 * Creates an *or* boolean clause for the query
 * @method or
 * @param {Object} clauses A variable number of *where* clauses to apply
 * @example
 *     or(where('x','=',10),where('y','=',1))
 * @return {Object}
 */
neon.query.or = function (clauses) {
    return new neon.query.BooleanClause('or', neon.util.ArrayUtils.argumentsToArray(arguments));
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
neon.query.withinDistance = function (locationField, center, distance, distanceUnit) {
    return new neon.query.WithinDistanceClause(locationField, center, distance, distanceUnit);
};

/**
 * Executes a query that returns the field names from the data set. This method executes synchronously.
 * @method getFieldNames
 * @param {String} databaseName The name of the database that holds this data
 * @param {String} tableName The table name whose fields are being returned
 * @param {Function} successCallback The callback to call when the field names are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.getFieldNames = function (databaseName, tableName, successCallback, errorCallback) {
    return neon.util.AjaxUtils.doGet(
        neon.query.queryUrl_('/services/queryservice/fieldnames?databaseName=' + databaseName + '&tableName=' + tableName),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Executes the specified query and fires the callback when complete
 * @method executeQuery
 * @param {neon.query.Query} query the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.executeQuery = function (query, successCallback, errorCallback) {
    var transform = query.filter.transform_;
    return neon.query.doExecuteQuery_(query, transform, successCallback, errorCallback, 'query');
};

/**
 * Executes the specified query group (a series of queries whose results are aggregate),
 * and fires the callback when complete
 * @method executeQueryGroup
 * @param {neon.query.QueryGroup} queryGroup the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.executeQueryGroup = function (queryGroup, successCallback, errorCallback) {
    return neon.query.doExecuteQuery_(queryGroup, queryGroup.transform_, successCallback, errorCallback, 'querygroup');
};

neon.query.doExecuteQuery_ = function (query, transform, successCallback, errorCallback, serviceName) {
    var queryParams = neon.query.buildQueryParamsString_(query, transform);
    return neon.util.AjaxUtils.doPostJSON(
        query,
        neon.query.queryUrl_('/services/queryservice/' + serviceName + '?' + queryParams),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


neon.query.buildQueryParamsString_ = function (query, transform) {
    var queryParams = 'includefiltered=' + query.includeFiltered_;
    if (transform) {
        queryParams += '&transform=' + transform.transformName;
        if (transform.transformParams) {
            transform.transformParams.forEach(function (param) {
                queryParams += '&param=' + encodeURIComponent(param);
            });
        }
    }
    return queryParams;
};


/**
 * Adds a filter to the data and fires the callback when complete
 * @method addFilter
 * @param {neon.query.Filter || neon.query.FilterProvider} filter The filter (or filter provider) to add. A filter provider can be useful
 * for dynamically creating more complex filters on the server based on subquery results
 * @param {Function} successCallback The callback to fire when the filter is added
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.addFilter = function (filter, successCallback, errorCallback) {
    var filterProvider = neon.query.wrapFilterInProvider_(filter);
    return neon.util.AjaxUtils.doPostJSON(
        filterProvider,
        neon.query.queryUrl_('/services/queryservice/addfilter'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Removes a filter from the data and fires the callback when complete
 * @method removeFilter
 * @param {String} filterId The id of the filter to remove
 * @param {Function} successCallback The callback to fire when the filter is removed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.removeFilter = function (filterId, successCallback, errorCallback) {
    var opts = {
        contentType: 'text/plain',
        responseType: 'json',
        success: successCallback,
        error: errorCallback
    };

    return neon.util.AjaxUtils.doPost(neon.query.queryUrl_('/services/queryservice/removefilter/' + filterId), opts);
};

/**
 * Replaces a filter and fires the callback when complete
 * @method replaceFilter
 * @param {String} filterId The id of the filter to replace
 * @param {neon.query.Filter || neon.query.FilterProvider} filter The filter (or filter provider) to add. A filter provider can be useful
 * for dynamically creating more complex filters on the server based on subquery results
 * @param {Function} successCallback The callback to fire when the replacement is complete
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.replaceFilter = function (filterId, filter, successCallback, errorCallback) {
    var filterProvider = neon.query.wrapFilterInProvider_(filter);
    return neon.util.AjaxUtils.doPostJSON(
        filterProvider,
        neon.query.queryUrl_('/services/queryservice/replacefilter/' + filterId),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

neon.query.wrapFilterInProvider_ = function (filter) {
    return filter instanceof neon.query.FilterProvider ? filter : new neon.query.SimpleFilterProvider(filter);
};

/**
 * Removes all filters from the data
 * @method clearFilters
 * @param {Function} successCallback The callback to fire when the filters are cleared
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.clearFilters = function (successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPost(
        neon.query.queryUrl_('/services/queryservice/clearfilters'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Sets the items that match the specified query to be selected
 * @method setSelectionWhere
 * @param {neon.query.Filter} filter The filter to match the items
 * @param {Function} successCallback The callback to execute when selection is completed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.setSelectionWhere = function (filter, successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPostJSON(
        filter,
        neon.query.queryUrl_('/services/queryservice/setselectionwhere'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Gets the items that are selected and match this query
 * @method getSelectionWhere
 * @param {neon.query.Filter} filter The filter to match items
 * @param {Function} successCallback The callback to execute when the selected items have been retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.getSelectionWhere = function (filter, successCallback, errorCallback) {
    var queryParams = '';
    if (filter.transform_) {
        queryParams += '?transform=' + filter.transform_;
    }
    return neon.util.AjaxUtils.doPostJSON(
        filter,
        neon.query.queryUrl_('/services/queryservice/getselectionwhere' + queryParams),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


/**
 * Sets the items with the specified ids to be selected
 * @method setSelectedIds
 * @param {Array} ids An array of ids of items to select
 * @param {Function} successCallback The callback to execute when selection is completed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.setSelectedIds = function (ids, successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPostJSON(
        ids,
        neon.query.queryUrl_('/services/queryservice/setselectedids'),
        {
            success: successCallback,
            error: errorCallback
        }

    );
};

/**
 * Adds the items with the specified ids to the current selection
 * @method addSelectedIds
 * @param {Array} ids An array of ids of items to add to the selection
 * @param {Function} successCallback The callback to execute when selection is completed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.addSelectedIds = function (ids, successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPostJSON(
        ids,
        neon.query.queryUrl_('/services/queryservice/addselectedids'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Adds the items with the specified ids to the current selection
 * @method addSelectedIds
 * @param {Array} ids An array of ids of items to add to the selection
 * @param {Function} successCallback The callback to execute when selection is completed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.removeSelectedIds = function (ids, successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPostJSON(
        ids,
        neon.query.queryUrl_('/services/queryservice/removeselectedids'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Clears the current selection
 * @method clearSelection
 * @param {Function} successCallback The callback to execute when the selection is cleared
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.clearSelection = function (successCallback, errorCallback) {
    return neon.util.AjaxUtils.doPost(
        neon.query.queryUrl_('/services/queryservice/clearselection'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

neon.query.queryUrl_ = function (path) {
    return neon.query.SERVER_URL + path;
};

/**
 * Creates a filter that can be applied to a dataset
 * @namespace neon.query
 * @class Filter
 * @constructor
 */
neon.query.Filter = function () {

    /*jshint expr: true */
    this.whereClause;
    this.transform_;
};

/**
 * Sets the *select* clause of the filter to select data from the specified dataset
 * @method selectFrom
 * @param {String} databaseName The name of the database that contains the data
 * @param {String} tableName The table to select from
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.selectFrom = function (databaseName, tableName) {
    this.databaseName = databaseName;
    this.tableName = tableName;
    return this;
};


/**
 * Adds a *where* clause to the filter.
 * @param {Object} arguments See {{#crossLink "neon.query.Query/where"}}{{/crossLink}} for documentation on how to structure the parameters
 * @method where
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.where = function () {
    if (arguments.length === 3) {
        this.whereClause = neon.query.where(arguments[0], arguments[1], arguments[2]);
    }
    else {
        // must be a boolean/geospatial clause
        this.whereClause = arguments[0];
    }
    return this;
};

/**
 * Adds a *withinDistance* clause to the filter.
 * @param {String} locationField The name of the field containing the location value
 * @param {neon.util.LatLon} center The point from which the distance is measured
 * @param {double} distance The maximum distance from the center point a result must be within to be returned in the query
 * @param {String} distanceUnit The unit of measure for the distance. See the constants in this class.
 * @method withinDistance
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.withinDistance = function (locationField, center, distance, distanceUnit) {
    return this.where(neon.query.withinDistance(locationField, center, distance, distanceUnit));
};

/**
 * Specifies a name of a transform to apply to the json before returning it from the query
 * @method transform
 * @param {String} transformName
 * @param {Array} [transformParams]
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.transform = function (transformName, transformParams) {
    this.transform_ = new neon.query.Transform(transformName, transformParams);
    return this;
};


/**
 * This is the parent class of objects used to create filters to be applied to the data. A filter provider
 * is passed to the server rather than a filter directly because some filter providers may dynamically create
 * filters on the server side.
 * @namespace neon.query
 * @class FilterProvider
 * @constructor
 * @param {neon.query.Filter} filter
 */
neon.query.FilterProvider = function (filter) {
    this.filter = filter;
};

/**
 * A filter provider that dynamically creates a filter on the server based on the
 * results matched by the subfilter. The newly generated filter will be structured as
 * follows:<br>
 *     &lt;fieldName&gt; &lt;operator&gt; &lt;subfilter-results&gt;
 *
 * For example, this can be used to generate a filter that matches any rows whose field X appears
 * in the results of the subfilter
 * @namespace neon.query
 * @class SubfilterFieldProvider
 * @param {neon.query.Filter} subfilter The subfilter whose results will be used as the basis for the newly generated filter
 * @param {String} fieldName The field used as the key of the generated filter
 * @param {String} operator The operator to use to compare the field to the results of the subfilter
 * @constructor
 */
neon.query.SubfilterFieldProvider = function (subfilter, fieldName, operator) {
    this.type = 'subfilter';
    this.operator = operator;
    this.fieldName = fieldName;
    neon.query.FilterProvider.call(this, subfilter);
};
// TODO: NEON-73 (Javascript inheritance library)
neon.query.SubfilterFieldProvider.prototype = new neon.query.FilterProvider();


/**
 * A generic function that can be applied to a field (on the server side). For example, this could be an aggregation
 * function such as an average or it could be a function to manipulate a field value such as extracting the month
 * part of a date
 * @param {String} operation The name of the operation to perform
 * @param {String} field The name of the field to perform the operation on
 * @param {String} name The name of the field created by performing this operation
 * @namespace neon.query
 * @class FieldFunction
 * @constructor
 * @private
 */
neon.query.FieldFunction = function (operation, field, name) {
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
 * @namespace neon.query
 * @class GroupByFunctionClause
 * @constructor
 */
neon.query.GroupByFunctionClause = function (operation, field, name) {
    this.type = 'function';
    neon.query.FieldFunction.call(this, operation, field, name);
};
// TODO: NEON-73 (Javascript inheritance library)
neon.query.GroupByFunctionClause.prototype = new neon.query.FieldFunction();


// These are not meant to be instantiated directly but rather by helper methods

neon.query.GroupBySingleFieldClause = function (field) {
    this.type = 'single';
    this.field = field;
};

neon.query.BooleanClause = function (type, whereClauses) {
    this.type = type;
    this.whereClauses = whereClauses;
};

neon.query.WhereClause = function (lhs, operator, rhs) {
    this.type = 'where';
    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;

};

neon.query.DistinctClause = function (fieldName) {
    this.fieldName = fieldName;
};

neon.query.SortClause = function (fieldName, sortOrder) {
    this.fieldName = fieldName;
    this.sortOrder = sortOrder;
};

neon.query.LimitClause = function (limit) {
    this.limit = limit;
};

neon.query.WithinDistanceClause = function (locationField, center, distance, distanceUnit) {
    this.type = 'withinDistance';
    this.locationField = locationField;
    this.center = center;
    this.distance = distance;
    this.distanceUnit = distanceUnit;
};

neon.query.Transform = function (transformName, transformParams) {
    this.transformName = transformName;
    this.transformParams = transformParams;
};

/**
 * A filter provider that wraps a simple filter
 * @private
 * @namespace neon.query
 * @class SimpleFilterProvider
 * @param {neon.query.Filter} filter The filter being wrapped
 * @constructor
 */
neon.query.SimpleFilterProvider = function (filter) {
    this.type = 'simple';
    neon.query.FilterProvider.call(this, filter);
};
// TODO: NEON-73 (Javascript inheritance library)
neon.query.SimpleFilterProvider.prototype = new neon.query.FilterProvider();


/**
 * A query group is one that encompasses multiple queries. The results of all queries in the group are
 * combined into a single json object (the resulting json object has one key for each of the queries where the
 * value of each key is the result of that query). The entire query result can also be sent to a transform
 * service to manipulate all of the results at once
 * @constructor
 * @class QueryGroup
 */
neon.query.QueryGroup = function () {
    this.namedQueries = [];

    /*jshint expr: true */
    this.transform_;
    this.includeFiltered_ = false;
};

/**
 * Adds a query to execute as part of the query group
 * @method addQuery
 * @param {String} name The name associated with this query in the group (the name is used as the key in the resulting json)
 * @param {neon.query.Query} query The query to execute as part of the query group
 * @return {neon.query.QueryGroup} This object
 */
neon.query.QueryGroup.prototype.addQuery = function (name, query) {
    this.namedQueries.push(new neon.query.NamedQuery(name, query));
    return this;
};

// TODO: When NEON-170 is complete, update documentation indicating that individual query settings of trasnform are ignored
/**
 * Sets the transform to execute on the results of the query group (individual query settings of transform are
 * ignored, and all queries in the group will use this value).
 * See {{#crossLink "neon.query.Query/transform"}}{{/crossLink}} for parameter details
 * @method transform
 * @param {String} transformName
 * @param {Array} transformParams
 * @return {neon.query.QueryGroup} This query group
 */
neon.query.QueryGroup.prototype.transform = function (transformName, transformParams) {
    this.transform_ = new neon.query.Transform(transformName, transformParams);
    return this;
};

// TODO: When NEON-170 is complete, update documentation indicating that individual query settings of includedFiltered are ignored
/**
 * Sets whether or not the results of the query should include data that is filtered out. When true,
 * all data, regardless of filters, will be included in the query (individual query settings of includeFiltered are
 * ignored, and all queries in the group will use this value).
 * See {{#crossLink "neon.query.Query/includeFiltered"}}{{/crossLink}} for parameter details
 * @method includeFiltered
 * @param {Boolean} includeFiltered
 * @return {neon.query.QueryGroup} This query group
 */
neon.query.QueryGroup.prototype.includeFiltered = function (includeFiltered) {
    this.includeFiltered_ = includeFiltered;
    return this;
};


/**
 * A named query is one that is included as part of a query group. Each query is executed as part of the group, and
 * the name is used to indicate the json key to use for its results
 * @param name
 * @param query
 * @class NamedQuery
 * @constructor
 * @private
 */
neon.query.NamedQuery = function (name, query) {
    this.name = name;
    this.query = query;
};