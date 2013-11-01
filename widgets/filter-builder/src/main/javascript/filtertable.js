var neon = neon || {};
neon.filter = (function () {

    var filterKey;
    var columnOptions;
    var operatorOptions = ["=", "!=", ">", "<", ">=", "<="];
    var filterState = {
        data: []
    };

    var FilterRow = function (columnValue, operatorValue, value) {
        this.columnOptions = columnOptions;
        this.columnValue = columnValue;
        this.operatorOptions = operatorOptions;
        this.operatorValue = operatorValue;
        this.value = value;
        this.submittable = false;
    };

    var addFilter = function (id) {
        var updatingExisting = filterState.data[id].submittable;
        updateDataFromForm(id);
        var filter = buildFilterFromData();

        neon.eventing.publishing.replaceFilter(filterKey, filter, function(){
            if (!updatingExisting) {
                filterState.data.push(new FilterRow());
            }
            redrawFilterContentAndSaveState();
        });
    };

    var removeFilter = function (id) {
        filterState.data.splice(id, 1);
        var filter = buildFilterFromData();

        neon.eventing.publishing.replaceFilter(filterKey, filter, redrawFilterContentAndSaveState);
    };

    var initializeFilterSection = function () {
        filterState.data = [];
        filterState.data.push(new FilterRow());
        setFilterDisplay();
        redrawFilterContentAndSaveState();
    };

    var setFilterKey = function (key) {
        filterKey = key;
    };

    var setColumns = function (columns){
        columnOptions = columns;
    };

    var getFilterKey = function () {
        return filterKey;
    };

    var getFilterState = function () {
        return filterState;
    };

    var setFilterState = function (state) {
        filterState = state;
        setColumns(state.data[0].columnOptions);
        setFilterDisplay();
        setFilterContentFromFilterState();
    };


    function buildCompoundWhereClause(data) {
        var whereClause;
        var clauses = [];
        var selected = $("input[type='radio'][name='boolean']:checked").val();

        $.each(data, function (index, filterData) {
            var clause = neon.query.where(filterData.columnValue, filterData.operatorValue, filterData.value);
            clauses.push(clause);
        });

        if (selected === "AND") {
            whereClause = neon.query.and.apply(this, clauses);
        }
        else {
            whereClause = neon.query.or.apply(this, clauses);
        }
        return whereClause;
    }

    function buildFilterFromData() {
        var dataset = neon.wizard.getDataset();
        var baseFilter = new neon.query.Filter().selectFrom(dataset.database, dataset.table);
        var data = getSubmittableData();

        var whereClause;
        if (data.length === 0) {
            return baseFilter;
        }
        if (data.length === 1) {
            var filterData = data[0];
            whereClause = neon.query.where(filterData.columnValue, filterData.operatorValue, filterData.value);
        }
        else {
            whereClause = buildCompoundWhereClause.call(this, data);
        }
        return baseFilter.where(whereClause);
    }

    function getSubmittableData() {
        var data = [];
        $.each(filterState.data, function (index, value) {
            if (value.submittable) {
                data.push(value);
            }
        });

        return data;
    }

    function updateDataFromForm(id) {
        var filterData = filterState.data[id];
        filterData.columnValue = $('#column-select-' + id + ' option:selected').val();
        filterData.operatorValue = $('#operator-select-' + id + ' option:selected').val();
        filterData.value = $('#value-input-' + id).val();
        filterData.submittable = true;

        if ($.isNumeric(filterData.value)) {
            filterData.value = parseFloat(filterData.value);
        }
        if (filterData.value === "null" || filterData.value === "") {
            filterData.value = null;
        }
        if (filterData.value === '""') {
            filterData.value = "";
        }
    }

    function redrawFilterContentAndSaveState() {
        setFilterContentFromFilterState();
        neon.filterBuilderState.saveState();
    }

    function setFilterContentFromFilterState() {
        var source = $("#filters").html();
        var template = Handlebars.compile(source);
        var html = template(filterState);
        $('#filter-content').html(html);
    }

    function setFilterDisplay() {
        if (columnOptions.length !== 0) {
            $("#filter-container").show();
            $("#clear-filters-button").show();
        }
        else {
            $("#filter-container").hide();
            $("#clear-filters-button").hide();
        }
    }

    return {
        addFilter: addFilter,
        removeFilter: removeFilter,
        setFilterKey: setFilterKey,
        setFilterState: setFilterState,
        setColumns: setColumns,
        getFilterKey: getFilterKey,
        getFilterState: getFilterState,
        initializeFilterSection: initializeFilterSection
    };

})();

