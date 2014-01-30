

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
    this.disregardFilters_ = false;
    this.selectionOnly_ = false;

    this.groupByClauses = [];
    this.isDistinct = false;
    this.aggregates = [];
    this.sortClauses = [];
    this.limitClause = undefined;
    this.transform = undefined;
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
    this.fields = neon.util.arrayUtils.argumentsToArray(arguments);
    return this;
};

/**
 * Sets the *where* clause of the query to determine how to select the data
 * @method where
 * The arguments can be in either multiple formats<br>
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

    var list = neon.util.arrayUtils.argumentsToArray(arguments);
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
 * @param [{String}] name The name of the new field generated by this operation. If not specified, a name
 * will be generated based on the operation name and the field name
 * @method aggregate
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.aggregate = function (aggregationOperation, aggregationField, name) {
    var newFieldName = name != null ? name : (aggregationOperation + '(' + aggregationField + ')');
    this.aggregates.push(new neon.query.FieldFunction(aggregationOperation, aggregationField, newFieldName));
    return this;
};

/**
 * Specifies the query return distinct results
 * @method distinct
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.distinct = function () {
    this.isDistinct = true;
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
 * Specifies an offset to start the results from. This can be used in combination with limit for pagination
 * @method offset
 * @param {int} offset The number of rows to skip in the results
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.offset = function (offset) {
    this.offsetClause = new neon.query.OffsetClause(offset);
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
    var list = neon.util.arrayUtils.argumentsToArray(arguments);
    for (var i = 0; i < list.length; i += 2) {
        var field = list[i];
        var order = list[i + 1];
        this.sortClauses.push(new neon.query.SortClause(field, order));
    }
    return this;
};

/**
 * Adds a transform to this query
 * @method setTransform
 * @param {neon.query.Transform} transformObj a transform to be applied to the data
 * @return {neon.query.Query} This query object
 */
neon.query.Query.prototype.setTransform = function(transformObj){
    this.transform = transformObj;
    return this;
};

/**
 * Sets the query mode to return all data. This ignores the current filters and selection.
 * @method allDataMode
 * @return {neon.query.Query} This query for method chaining
 */
neon.query.Query.prototype.allDataMode = function () {
    this.disregardFilters_ = true;
    this.selectionOnly_ = false;
    return this;
};

/**
 * Sets the query mode to return all data. This applies the current filters and ignores the selection.
 * @method filteredMode
 * @return {neon.query.Query} This query for method chaining
 */
neon.query.Query.prototype.filteredMode = function () {
    this.disregardFilters_ = false;
    this.selectionOnly_ = false;
    return this;
};

/**
 * Sets the query mode to return just the current selection. Selected items will be returned after the current filters are applied.
 * @method selectionMode
 * @return {neon.query.Query} This query for method chaining
 */
neon.query.Query.prototype.selectionMode = function () {
    this.disregardFilters_ = false;
    this.selectionOnly_ = true;
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
    return new neon.query.BooleanClause('and', neon.util.arrayUtils.argumentsToArray(arguments));
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
    return new neon.query.BooleanClause('or', neon.util.arrayUtils.argumentsToArray(arguments));
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
 * @param {String} widgetName The widget name who is requesting the field names. One of {neon.widget}
 * @param {Function} successCallback The callback to call when the field names are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.getFieldNames = function (databaseName, tableName, widgetName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('queryservice', 'fields', '?databaseName=' + databaseName + '&tableName=' + tableName + '&widgetName=' + widgetName),
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
    return neon.query.executeQueryService_(query, successCallback, errorCallback, 'query');
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
    return neon.query.executeQueryService_(queryGroup, successCallback, errorCallback, 'querygroup');
};

neon.query.executeQueryService_ = function (query, successCallback, errorCallback, serviceName) {
    if(query.selectionOnly_){
        serviceName += "withselectiononly";
    }
    else if (query.disregardFilters_) {
        serviceName += "disregardfilters";
    }

    return neon.util.ajaxUtils.doPostJSON(
        query,
        neon.query.serviceUrl('queryservice', serviceName),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


/**
 * Registers for a filter key and fires the callback when complete
 * @method registerForFilterKey
 * @param {String} databaseName The database name against which the filter is registered
 * @param {String} tableName The table name against which the filter is registered
 * @param {Function} successCallback The callback to fire when registration succeeds
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.registerForFilterKey = function (databaseName, tableName, successCallback, errorCallback) {
    var dataSet = {
        databaseName: databaseName,
        tableName: tableName
    };

    return neon.util.ajaxUtils.doPostJSON(
        dataSet,
        neon.query.serviceUrl('filterservice', 'registerforfilterkey'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


/**
 * Adds a filter to the data and fires the callback when complete
 * @method addFilter
 * @param {Object} filterKey The object returned when registering the filter must be used here
 * @param {neon.query.Filter} filter The filter to be added
 * @param {Function} successCallback The callback to fire when the filter is added
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.addFilter = function (filterKey, filter, successCallback, errorCallback) {
    var filterContainer = {
        filterKey: filterKey,
        filter: filter
    };

    return neon.util.ajaxUtils.doPostJSON(
        filterContainer,
        neon.query.serviceUrl('filterservice', 'addfilter'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Removes a filter from the data and fires the callback when complete
 * @method removeFilter
 * @param {Object} filterKey The object returned when registering the filter must be used here
 * @param {Function} successCallback The callback to fire when the filter is removed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.removeFilter = function (filterKey, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.query.serviceUrl('filterservice', 'removefilter'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Replaces a filter and fires the callback when complete
 * @method replaceFilter
 * @param {Object} filterKey The object returned when registering the filter must be used here
 * @param {neon.query.Filter} filter The filter that is a replacement for the previous filter
 * @param {Function} successCallback The callback to fire when the replacement is complete
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.replaceFilter = function (filterKey, filter, successCallback, errorCallback) {
    var filterContainer = {
        filterKey: filterKey,
        filter: filter
    };

    return neon.util.ajaxUtils.doPostJSON(
        filterContainer,
        neon.query.serviceUrl('filterservice', 'replacefilter'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Removes all filters from the data
 * @method clearFilters
 * @param {Function} successCallback The callback to fire when the filters are cleared
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.clearFilters = function (successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('filterservice', 'clearfilters'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Adds a selection to the data and fires the callback when complete
 * @method addSelection
 * @param {Object} filterKey The object returned when registering the filter must be used here
 * @param {neon.query.Filter} filter The filter to be added
 * @param {Function} successCallback The callback to fire when the selection is added
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.addSelection = function (filterKey, filter, successCallback, errorCallback) {
    var filterContainer = {
        filterKey: filterKey,
        filter: filter
    };

    return neon.util.ajaxUtils.doPostJSON(
        filterContainer,
        neon.query.serviceUrl('selectionservice', 'addselection'),
        {
            success: successCallback,
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Removes a selection from the data and fires the callback when complete
 * @method removeSelection
 * @param {Object} filterKey The object returned when registering for a key must be used here
 * @param {Function} successCallback The callback to fire when the selection is removed
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.removeSelection = function (filterKey, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.query.serviceUrl('selectionservice', 'removeselection'),
        {
            success: successCallback,
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Replaces a selection and fires the callback when complete
 * @method replaceSelection
 * @param {Object} filterKey The object returned when registering for a key must be used here
 * @param {neon.query.Filter} filter The filter that is a replacement for the previous selection
 * @param {Function} successCallback The callback to fire when the replacement is complete
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.replaceSelection = function (filterKey, filter, successCallback, errorCallback) {
    var filterContainer = {
        filterKey: filterKey,
        filter: filter
    };

    return neon.util.ajaxUtils.doPostJSON(
        filterContainer,
        neon.query.serviceUrl('selectionservice', 'replaceselection'),
        {
            success: successCallback,
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Removes all filters from the selection
 * @method clearSelection
 * @param {Function} successCallback The callback to fire when the selection is cleared
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.clearSelection = function (successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('selectionservice', 'clearselection'),
        {
            success: successCallback,
            error: errorCallback,
            global: false
        }
    );
};


/**
 * Submits a text based query to the server.
 * @method submitTextQuery
 * @param {String} queryText The query text to be submitted
 * @param {Function} successCallback The callback to execute when the query is parsed, which contains the query result
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.submitTextQuery = function (queryText, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('languageservice', 'query'),
        {
            data: { text: queryText },
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            responseType: 'json',
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Save the current state of a widget.
 * @method saveState
 * @param {String} id a unique identifier of a client widget
 * @param {Object} stateObject an object that is to be saved.
 * @param {Function} successCallback The callback to execute when the state is saved. The callback will have no data.
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.saveState = function (id, stateObject, successCallback, errorCallback) {
    var strObject = JSON.stringify(stateObject);
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('widgetstateservice', 'savestate'),
        {
            data: { clientId: id, state: strObject},
            success: successCallback,
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Gets the current state that has been saved.
 * @method getSavedState
 * @param {String} id an unique identifier of a client widget
 * @param {Function} successCallback The callback that contains the saved data.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.getSavedState = function (id, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('widgetstateservice', 'restoreState', '?clientId=' + id),
        {
            success: function (data) {
                if (!data) {
                    return;
                }
                if (successCallback && typeof successCallback === 'function') {
                    successCallback(data);
                }
            },
            error: function () {
                //Do nothing, the state does not exist.
            }
        }
    );
};

/**
 * Gets widget initialization metadata.
 * @method getWidgetInitialization
 * @param {String} id an identifier of a widget, usually the widget name
 * @param {Function} successCallback The callback that contains the saved data.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.getWidgetInitialization = function (id, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('widgetstateservice', 'widgetinitialization', '?widget=' + id),
        {
            success: successCallback
        }
    );
};

/**
 * Connects a user to the given datastore.
 * @method connectToDatastore
 * @param {String} datastore The name of the datastore (e.g. mongo or hive)
 * @param {String} hostname The connection url of the datastore
 * @param {Function} successCallback Callback invoked on success
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connectToDatastore = function (datastore, hostname, successCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('connectionservice', 'connect'),
        {
            data: { datastore: datastore, hostname: hostname },
            success: successCallback
        }
    );
};

/**
 * Gets the database names available for the current datastore
 * @method getDatabaseNames
 * @param {Function} successCallback The callback that contains the database names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.getDatabaseNames = function (successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('queryservice', 'databasenames'),
        {
            success: successCallback
        }
    );
};

/**
 * Gets the tables names available for the current datastore and database
 * @method getTableNames
 * @param {String} databaseName The name of the database
 * @param {Function} successCallback The callback that contains the table names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.getTableNames = function (databaseName, successCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('queryservice', 'tablenames'),
        {
            data: { database: databaseName },
            success: successCallback
        }
    );
};

neon.query.serviceUrl = function (servicePath, serviceName, queryParamsString) {
    if (!queryParamsString) {
        queryParamsString = '';
    }

    return neon.query.SERVER_URL + '/services/' + servicePath + '/' + serviceName + queryParamsString;
};

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

neon.query.SortClause = function (fieldName, sortOrder) {
    this.fieldName = fieldName;
    this.sortOrder = sortOrder;
};

neon.query.LimitClause = function (limit) {
    this.limit = limit;
};

neon.query.OffsetClause = function (offset) {
    this.offset = offset;
};


neon.query.WithinDistanceClause = function (locationField, center, distance, distanceUnit) {
    this.type = 'withinDistance';
    this.locationField = locationField;
    this.center = center;
    this.distance = distance;
    this.distanceUnit = distanceUnit;
};
