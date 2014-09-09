/*
 * Copyright 2014 Next Century Corporation
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

var neon = neon || {};
neon.query = neon.query || {};

/**
 * FilterTable manages a set of filter clauses to be used with Neon queries
 * where each row in the table represents a separate filter clause.  A single row
 * represents a field-operator-value combo.  For example, "total < 10".  This class
 * provides convenience functions for adding/removing rows or building a filter
 * table from more primitive data arrays.
 * 
 * @example
 *    var filterRow = new FilterRow("total", "<", 10);<br>
 *    var filterTable = new FilterTable();<br>
 *    filterTable.addFilterRow(filterRow);
 * 
 * @class neon.query.FilterTable
 * @constructor
 */
neon.query.FilterTable = function () {
    //this.messenger = new neon.eventing.Messenger();
    this.filterKey = '';
    this.columnOptions;
    this.operatorOptions = ["=", "!=", ">", "<", ">=", "<=", "contains"];
    this.filterState = {
        data: []
    };
};

/**
 * Adds a FilterRow to the table.
 * @param {neon.query.FilterRow} row
 * @method addFilterRow
 */
neon.query.FilterTable.prototype.addFilterRow = function(row) {
    this.filterState.data.push(row);
}

/**
 * Inserts a FilterRow at a particular index in the table.
 * @param {neon.query.FilterRow} row
 * @param {Number} index
 * @method insertFilterRow
 */
neon.query.FilterTable.prototype.insertFilterRow = function(row, index) {
    this.filterState.data.splice(index, 1, row);
}

/**
 * Removes a FilterRow from the given row index and returns it.
 * @param {Number} id
 * @return {neon.query.FilterRow}
 * @method removeFilterRow
 */
neon.query.FilterTable.prototype.removeFilterRow = function (id) {
    return this.filterState.data.splice(id, 1);
};

/**
 * Returns the FilterRow at the given index.
 * @param {Number} id
 * @return {neon.query.FilterRow}
 * @method getFilterRow
 */
neon.query.FilterTable.prototype.getFilterRow = function (id) {
    return this.filterState.data[id];
};

/**
 * Sets the FilterRow at the given row index.
 * @param {neon.query.FilterRow} row 
 * @param {Number} index
 * @return {neon.query.FilterRow}
 * @method setFilterRow
 */
neon.query.FilterTable.prototype.setFilterRow = function (row, index) {
    return this.filterState.data[index] = row;
};

/**
 * Clears the filter table's state, removing all rows.
 * @method clearFilterState
 */
neon.query.FilterTable.prototype.clearFilterState = function () {
    this.filterState.data = [];
};

/**
 * Sets a filter key to use with these filter clauses
 * @param {String} key
 * @method setFilterKey
 */
neon.query.FilterTable.prototype.setFilterKey = function (key) {
    this.filterKey = key;
};

/**
 * Sets the list of valid column names for a FilterRow
 * @param {Array} columns An array of column names
 * @deprecated
 * @method setColumns
 */
neon.query.FilterTable.prototype.setColumns = function (columns){
    this.columnOptions = columns;
};

/**
 * Returns the current filter key associated with this filter table.
 * @return {String}
 * @method getFilterKey
 */
neon.query.FilterTable.prototype.getFilterKey = function () {
    return this.filterKey;
};

/**
 * Returns the filter state, the interal array of filter rows.
 * @return {Object}  An object containing a data array of FilterRows.
 * @method getFilterState
 */
neon.query.FilterTable.prototype.getFilterState = function () {
    return this.filterState;
};

/** 
 * Builds a Neon where clause suitable for use as a composite Filter for Neon Queries from the
 * FilterRow data contained in this FilterTable.
 * @param {String} database The database to filter.
 * @param {String} table The table to filter.
 * @param {Boolean} andClauses True if the compound clause should 'AND' all the FilterRows; false
 *    if it should 'OR' all the FilterRows
 * @return {neon.query.where}
 * @method buildFilterFromData
 */
neon.query.FilterTable.prototype.buildFilterFromData = function(database, table, andClauses) {
    var baseFilter = new neon.query.Filter().selectFrom(database, table);

    var whereClause;
    if (this.filterState.data.length === 0) {
        return baseFilter;
    }
    if (this.filterState.data.length === 1) {
        var filterData = this.filterState.data[0];
        whereClause = neon.query.where(filterData.columnValue, filterData.operatorValue, neon.query.FilterTable.parseValue(filterData.value));
    }
    else {
        whereClause = neon.query.FilterTable.buildCompoundWhereClause(this.filterState.data, andClauses);
    }
    return baseFilter.where(whereClause);
};

/**
 * Takes an array of FilterRows and builds a compound Neon where object suitable for 
 * filtering Neon Queries.
 * @param {Array} data A data array of FilterRows 
 * @param {Boolean} andClauses True if the compound clause should 'AND' all the FilterRows; false
 *    if it should 'OR' all the FilterRows
 * @return {neon.query.where}
 * @method buildCompoundWhereClause
 * @static
 */
neon.query.FilterTable.buildCompoundWhereClause = function(data, andClauses) {
    var whereClause;
    var clauses = [];

    $.each(data, function (index, filterData) {
        var clause = neon.query.where(filterData.columnValue, filterData.operatorValue, neon.query.FilterTable.parseValue(filterData.value));
        clauses.push(clause);
    });

    if (andClauses) {
        whereClause = neon.query.and.apply(this, clauses);
    }
    else {
        whereClause = neon.query.or.apply(this, clauses);
    }
    return whereClause;
};

/**
 * Takes a string value (e.g., input field value) and parses it to a float, null, or boolean, or string as
 * appropriate to work with the Neon Query API.
 * @param {String} value The value to parse
 * @return {String|Number|Boolean|null} 
 * @method parseValue
 * @static
 */
neon.query.FilterTable.parseValue = function(value) {
    var retVal = value;

    if ($.isNumeric(retVal)) {
       retVal = parseFloat(retVal);
    }
    else if (retVal === "null" || retVal === "") {
       retVal = null;
    }
    else if (retVal === '""') {
       retVal = "";
    }
    else if (retVal === 'false') {
       retVal = false;
    }
    else if (retVal === 'true') {
       retVal = true;
    }
    return retVal;
};

/**
 * A FilterRow is a basic support object for a filter build application.  It store the 
 * minimum data elements required to build a Neon filter: a column to act upon, the operator
 * for comparison, and a value to compare against.
 * 
 * @example
 *    var filterRow = new FilterRow("total", "<", 10);
 * 
 * @class neon.query.FilterRow
 * @constructor
 */
neon.query.FilterRow = function (columnValue, operatorValue, value, columnOptions, operatorOptions) {
    this.columnOptions = columnOptions;
    this.columnValue = columnValue;
    this.operatorOptions = operatorOptions;
    this.operatorValue = operatorValue;
    this.value = value;
};