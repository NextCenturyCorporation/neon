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

/**
 * Creates a filter that can be applied to a dataset
 * @class neon.query.Filter
 * @constructor
 */
neon.query.Filter = function () {
    // the database name will automatically be populated by the connection when a query is executed based
    // on what the "use" method provided as a database
    this.databaseName = undefined;
    this.whereClause = undefined;
};

/**
 * Sets the *select* clause of the filter to select data from the specified table
 * @method selectFrom
 * @param {String} tableName The table to select from. This may be fully qualified with [databaseName.]tableName, in
 * which case, the database specified in the "use" method of the connection will be overridden.
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.selectFrom = function (tableName) {
    var parts = tableName.split(".");
    if ( parts.length === 1 ) {
        this.tableName = parts[0];
    }
    else {
        this.databaseName = parts[0];
        this.tableName = parts[1];
    }
    return this;
};


/**
 * Adds a *where* clause to the filter.
 * See {{#crossLink "neon.query.Query/where"}}{{/crossLink}} for documentation on how to structure the parameters
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
 * @param {number} distance The maximum distance from the center point a result must be within to be returned in the query
 * @param {String} distanceUnit The unit of measure for the distance. See the constants in this class.
 * @method withinDistance
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.withinDistance = function (locationField, center, distance, distanceUnit) {
    return this.where(neon.query.withinDistance(locationField, center, distance, distanceUnit));
};
