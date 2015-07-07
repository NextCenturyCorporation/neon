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
neon.query.Filter = function() {
    this.filterName = undefined;
    this.databaseName = undefined;
    this.tableName = undefined;
    this.whereClause = undefined;
};

neon.query.Filter.prototype.name = function(name) {
    this.filterName = name;
    return this;
};

/**
 * Sets the *select* clause of the filter to select data from the specified table
 * @method selectFrom
 * @param {String} databaseName
 * @param {String} tableName Table to select from
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.selectFrom = function(databaseName, tableName) {
    this.databaseName = databaseName;
    this.tableName = tableName;
    return this;
};

/**
 * Adds a *where* clause to the filter.
 * See {{#crossLink "neon.query.Query/where"}}{{/crossLink}} for documentation on how to structure the parameters
 * @method where
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.where = function() {
    if(arguments.length === 3) {
        this.whereClause = neon.query.where(arguments[0], arguments[1], arguments[2]);
    } else {
        // single argument is a boolean or a geospacial clause
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
neon.query.Filter.prototype.withinDistance = function(locationField, center, distance, distanceUnit) {
    return this.where(neon.query.withinDistance(locationField, center, distance, distanceUnit));
};

neon.query.Filter.prototype.geoIntersection = function(locationField, points, geometryType) {
    return this.where(neon.query.withinDistance(locationField, points, geometryType));
};

neon.query.Filter.prototype.geoWithin = function(locationField, points) {
    return this.where(neon.query.withinDistance(locationField, points));
};

neon.query.Filter.getFilterState = function(databaseName, tableName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('filterservice', 'filters/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName)), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};
