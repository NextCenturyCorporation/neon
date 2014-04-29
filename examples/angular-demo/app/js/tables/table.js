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

var tables = tables || {};

/**
 * Creates a new table
 * @class Table
 * @namespace tables

 * @param {String} tableSelector The selector for the component in which the table will be drawn
 * @param {Object} opts A collection of key/value pairs used for configuration parameters:
 * <ul>
 *     <li>data (required) - An array of data to display in the table</li>
 *     <li>columns (optional) - A list of the fields to display in the table. If not specified, the table
 *     will iterate through all of the rows and get the unique column names. This can be a slow operation for
 *     large datasets.</li>
 *     <li>id (optional) - The name of the column with the unique id for the item. If this is not
 *     specified, an id field will be autogenerated and appended to the data (the original data
 *     will be modified)</li>
 *     <li>gridOptions (optional) - Slickgrid options may be set here.
 *     </li>
 * </ul>
 *
 * @constructor
 *
 * @example
 *     var data = [
 *                 {"field1": "aVal", "field2": 2},
 *                 {"field1": "bVal", "field2": 5, "anotherField" : "anotherVal"},
 *                ];
 *     var columns = ["field1","field2","anotherField"];
 *     var opts = { "data" : data, "columns" : columns };
 *     var table = new tables.Table('#table', opts).draw();
 * *
 */
tables.Table = function (tableSelector, opts) {
    this.tableSelector_ = tableSelector;
    this.idField_ = opts.id;
    this.options_ = opts.gridOptions || {};
    
    var data = opts.data;
    var columns = opts.columns ? opts.columns : tables.Table.computeColumnNames_(data);

    if (!this.idField_) {
        tables.Table.appendGeneratedId_(data);
        this.idField_ = tables.Table.AUTOGENERATED_ID_FIELD_NAME_;
    }

    this.dataView_ = this.createDataView_(data);
    this.columns_ = tables.Table.createSlickgridColumns_(columns);
};

tables.Table.AUTOGENERATED_ID_FIELD_NAME_ = '__autogenerated_id';


/**
 * Computes the column names by iterating through the data and saving the unique column names.
 * @param {Array} data The data being shown in the table
 * @return {Array} The unique column names
 * @method computeColumnNames_
 * @private
 */
tables.Table.computeColumnNames_ = function (data) {
    // just use an object to store the keys for faster lookup (basically a hash where we don't care about the value)
    var columns = {};
    data.forEach(function (row) {
        var keys = Object.keys(row);
        keys.forEach(function (key) {
            columns[key] = true;
        });
    });
    return Object.keys(columns);
};

/**
 * Creates the sort comparator to sort the data in the table
 * @param {String} field The field being sorted
 * @param {Object} sortInfo Information provided by the data view about the sort operation
 * @return {Function} A function to perform the sort comparison
 * @method sortComparator_
 * @private
 */
tables.Table.sortComparator_ = function (field, sortInfo) {
    return function (a, b) {
        var result = 0;
        if (a[field] > b[field]) {
            result = 1;
        }
        else if (a[field] < b[field]) {
            result = -1;
        }
        return sortInfo.sortAsc ? result : -result;
    };
};

tables.Table.appendGeneratedId_ = function (data) {
    var id = 0;
    data.forEach(function (el) {
        el[tables.Table.AUTOGENERATED_ID_FIELD_NAME_] = id++;
    });
};

/**
 * Creates a slickgrid data view from the raw data
 * @param {Array} data Array of data to be displayed
 * @method createDataView_
 * @private
 */
tables.Table.prototype.createDataView_ = function (data) {
    var dataView = new Slick.Data.DataView();
    dataView.setItems(data, this.idField_);
    return dataView;
};


/**
 * Converts a list of column names to the format required by the slickgrids library
 * used to create the tables
 * @param {Array} columnNames A list of column names
 * @method createSlickgridColumns_
 * @private
 */
tables.Table.createSlickgridColumns_ = function (columnNames) {
    var slickgridColumns = [];
    columnNames.forEach(function (col) {
        var slickgridColumn = {};
        slickgridColumn.id = col;
        slickgridColumn.name = col;
        slickgridColumn.field = col;
        slickgridColumn.sortable = true;
        slickgridColumn.formatter = tables.Table.defaultCellFormatter_;
        slickgridColumns.push(slickgridColumn);
    });
    return slickgridColumns;
};

tables.Table.defaultCellFormatter_ = function (row, cell, value, columnDef, dataContext) {
    // most of this taken from slick.grid.js defaultFormatter but modified to support nested objects
    if (value === null || value === undefined) {
        return "";
    }

    // check if nested object. if it is, append each of the key/value pairs
    var keys = tables.Table.getObjectKeys_(value);

    if (keys.length === 0) {
        return value;
    }

    return tables.Table.createKeyValuePairsString_(value, keys, row, cell, columnDef, dataContext);

};

tables.Table.getObjectKeys_ = function (object) {
    var keys = [];
    if (typeof object === 'object') {
        keys = Object.keys(object);
    }
    return keys;
};

tables.Table.createKeyValuePairsString_ = function (object, keys, row, cell, columnDef, dataContext) {
    var keyValueStrings = [];
    keys.forEach(function (key) {
        keyValueStrings.push(key + ': ' + tables.Table.defaultCellFormatter_(row, cell, object[key], columnDef, dataContext));
    });
    return keyValueStrings.join(', ');
};

/**
 * Draws the table in the selector specified in the constructor
 * @method draw
 * @return {tables.Table} This table
 */
tables.Table.prototype.draw = function () {
    this.table_ = new Slick.Grid(this.tableSelector_, this.dataView_, this.columns_, this.options_);
    this.addSortSupport_();
    this.table_.registerPlugin(new Slick.AutoTooltips({ enableForHeaderCells: true }));

    return this;
};

tables.Table.prototype.refreshLayout = function () {
    // the table may not be drawn yet when this is called (this method can be used as a hook to resize a table, but
    // if the browser is resized before the table is drawn, the table will be undefined here)
    if (this.table_) {
        this.table_.resizeCanvas();
    }
};

tables.Table.prototype.registerSelectionListener = function(callback){
    if(!callback || typeof callback !== 'function'){
        return;
    }
    var rowModel = new Slick.RowSelectionModel();
    this.table_.setSelectionModel(rowModel);

    var me = this;
    rowModel.onSelectedRangesChanged.subscribe(function (){
        if(me.idField_ === tables.Table.AUTOGENERATED_ID_FIELD_NAME_){
            return;
        }

        var selectedRowData = [];
        _.each(me.table_.getSelectedRows(), function(rowIndex){
            selectedRowData.push(me.table_.getDataItem(rowIndex));
        });
        callback(me.idField_, selectedRowData);
    });
    return this;
};

tables.Table.prototype.addSortSupport_ = function () {
    var me = this;
    this.table_.onSort.subscribe(function (event, args) {
        var field = args.sortCol.field;
        var data = me.dataView_.getItems();
        // use a stable sorting algorithm as opposed to the built-in
        // dataView sort which may not be stable
        data = data.mergeSort(tables.Table.sortComparator_(field, args));
        me.dataView_.setItems(data);
        me.table_.invalidateAllRows();
        me.table_.render();
    });
};