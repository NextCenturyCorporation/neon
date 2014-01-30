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

var neon = neon || {};
neon.filterBuilderState = (function () {

    var clientId;

    function restoreState(){
        neon.ready(function(){
            clientId = neon.eventing.messaging.getInstanceId();
            neon.query.getSavedState(clientId, function(data){
                restoreConnectionState(data);
                if(data.filterKey){
                    restoreConnectionAndFilterState(data);
                }
            });
        });
    }

    function restoreConnectionState(data){
        $("#db-table").show();
        $('#hostname-input').val(data.selectedHostname);

        //set database options
        neon.wizard.populateDropdown('#database-select', data.databases);
        //set table options
        neon.wizard.populateDropdown('#table-select', data.tables);

        neon.dropdown.setDropdownInitialValue("datastore-select", data.selectedDatastore);
        neon.dropdown.setDropdownInitialValue("database-select", data.selectedDatabase);
        neon.dropdown.setDropdownInitialValue("table-select", data.selectedTable);
    }

    function restoreConnectionAndFilterState(data){
        $("#filter-container").show();
        $("#clear-filters-button").show();
        neon.filterTable.setFilterKey(data.filterKey);
        neon.filterTable.setFilterState(data.filterState);
        $("input[type='radio'][name='boolean'][value=" + data.andOr + "]").attr('checked', 'checked');
    }

    function buildSimpleStateObject(){
        var dataset = neon.wizard.getDataset();
        var databaseOptions = $('#database-select option');
        var tableOptions = $('#table-select option');

        var stateObject = {
            selectedHostname: $('#hostname-input').val(),
            selectedDatastore: $('#datastore-select option:selected').val(),
            selectedDatabase: dataset.database,
            selectedTable: dataset.table,
            databases: $.map(databaseOptions, function(option){ return option.value;}),
            tables: $.map(tableOptions, function(option){ return option.value;})
        };

        return stateObject;
    }

    function buildFullStateObject(){
        var stateObject = buildSimpleStateObject();
        var selectedAndOr = $("input[type='radio'][name='boolean']:checked").val();

        $.extend(stateObject, {
            filterKey: neon.filterTable.getFilterKey(),
            filterState: neon.filterTable.getFilterState(),
            andOr: selectedAndOr
        });

        return stateObject;
    }

    function saveState(){
        if(neon.filterTable.getFilterKey()){
            neon.query.saveState(clientId, buildFullStateObject());
        }
        else{
            neon.query.saveState(clientId, buildSimpleStateObject());
        }
    }

    return {
        restoreState: restoreState,
        saveState: saveState
    };

})();