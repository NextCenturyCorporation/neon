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
    // the database name will automatically be populated by the connection when a query is executed based
    // on what the "use" method provided as a database
    this.databaseName = undefined;
    this.whereClause = undefined;
};

/**
 * Sets the *select* clause of the filter to select data from the specified table
 * @method selectFrom
 * @param {...String} args The database/table to select from. This may be the database and table name or just arg table
 * name, in which case the database specified in the "use" method of the connection will be added to the query when
 * it is executed. The table name may also be fully qualified with [databaseName.]tableName.
 * @return {neon.query.Filter} This filter object
 */
neon.query.Filter.prototype.selectFrom = function() {
    var parts = neon.util.arrayUtils.argumentsToArray(arguments);
    if(parts.length === 1) {
        // check for the fully qualified [database.]table
        var table = parts[0].split(".");
        if(table.length === 1) {
            this.tableName = table[0];
        } else {
            this.databaseName = table[0];
            this.tableName = table[1];
        }
    } else { // database and table passed in separately
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
neon.query.Filter.prototype.where = function() {
    if(arguments.length === 3) {
        this.whereClause = neon.query.where(arguments[0], arguments[1], arguments[2]);
    } else {
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
neon.query.Filter.prototype.withinDistance = function(locationField, center, distance, distanceUnit) {
    return this.where(neon.query.withinDistance(locationField, center, distance, distanceUnit));
};

neon.query.Filter.prototype.geoIntersection = function(locationField, points, geometryType) {
    return this.where(neon.query.withinDistance(locationField, points, geometryType));
};

neon.query.Filter.prototype.geoWithin = function(locationField, points) {
    return this.where(neon.query.withinDistance(locationField, points));
};

/**
 * Returns the field names from the left-hand-side of the where clauses in the given filter.
 * @param {Object} The Neon filter JSON object
 * @method getFieldNames
 * @return {Object} The map containing all field names in the given filter as keys
 */
neon.query.Filter.getFieldNames = function(filter) {
    var fieldNames = {};

    // Create a helper function to recursively iterate over an array of where clause objects.
    var helper = function(clauses) {
        if(clauses.length === 1) {
            if(clauses[0].whereClauses) {
                return helper(clauses[0].whereClauses);
            }
            return [clauses[0].lhs];
        }

        return helper(clauses.slice(0, 1)).concat(helper(clauses.slice(1)));
    };

    var fieldNamesArray = helper([filter.whereClause]);
    for(var i = 0; i < fieldNamesArray.length; ++i) {
        fieldNames[fieldNamesArray[i]] = true;
    }
    return fieldNames;
};
