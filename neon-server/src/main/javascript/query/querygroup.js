'use strict';
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
 * A query group is one that encompasses multiple queries. The results of all queries in the group are
 * combined into a single json object.
 * @constructor
 * @class neon.query.QueryGroup
 */
neon.query.QueryGroup = function() {
    this.queries = [];
    this.ignoreFilters_ = false;
    this.selectionOnly_ = false;
};

/**
 * Adds a query to execute as part of the query group
 * @method addQuery
 * @param {neon.query.Query} query The query to execute as part of the query group
 * @return {neon.query.QueryGroup} This object
 */
neon.query.QueryGroup.prototype.addQuery = function(query) {
    this.queries.push(query);
    return this;
};

/**
 * Sets the query to ignore any filters that are currently applied
 * @method ignoreFilters
 * @return {neon.query.QueryGroup} This query group object
 */
neon.query.QueryGroup.prototype.ignoreFilters = function() {
    this.ignoreFilters_ = true;
    return this;
};

/**
 * Sets the query to return just the current selection
 * @method selectionOnly
 * @return {neon.query.QueryGroup} This query group object
 */
neon.query.QueryGroup.prototype.selectionOnly = function() {
    this.selectionOnly_ = true;
    return this;
};
