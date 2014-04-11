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

neon.query.FilterTable = function () {
    //this.messenger = new neon.eventing.Messenger();
    this.filterKey = '';
    this.columnOptions;
    this.operatorOptions = ["=", "!=", ">", "<", ">=", "<="];
    this.filterState = {
        data: []
    };
};

neon.query.FilterRow = function (columnValue, operatorValue, value, columnOptions, operatorOptions) {
    this.columnOptions = columnOptions;
    this.columnValue = columnValue;
    this.operatorOptions = operatorOptions;
    this.operatorValue = operatorValue;
    this.value = value;
};

neon.query.FilterTable.prototype.addFilterRow = function(row) {
    this.filterState.data.push(row);
}

neon.query.FilterTable.prototype.insertFilterRow = function(row, index) {
    this.filterState.data.splice(index, 1, row);
}

neon.query.FilterTable.prototype.removeFilterRow = function (id) {
    return this.filterState.data.splice(id, 1);
};

neon.query.FilterTable.prototype.getFilterRow = function (id) {
    return this.filterState.data[id];
};

neon.query.FilterTable.prototype.setFilterRow = function (row, index) {
    return this.filterState.data[index] = row;
};

neon.query.FilterTable.prototype.clearFilterState = function () {
    this.filterState.data = [];
};

neon.query.FilterTable.prototype.setFilterKey = function (key) {
    this.filterKey = key;
};

neon.query.FilterTable.prototype.setColumns = function (columns){
    this.columnOptions = columns;
};

neon.query.FilterTable.prototype.getFilterKey = function () {
    return this.filterKey;
};

neon.query.FilterTable.prototype.getFilterState = function () {
    return this.filterState;
};

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
