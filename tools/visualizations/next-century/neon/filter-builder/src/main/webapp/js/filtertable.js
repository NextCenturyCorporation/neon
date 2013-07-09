var neon = neon || {};
neon.filter = function () {

    var messageHandler = {
        publishMessage: function(){}
    };

    if(typeof (OWF) !== "undefined"){
        OWF.ready(function () {
            messageHandler = new neon.eventing.MessageHandler();
        });
    }

    var filterId;
    var columnOptions;

    var operatorOptions = ["=","!=",">","<",">=","<="];

    var FilterRow = function (columnValue, operatorValue, value) {
        this.columnOptions = columnOptions;
        this.columnValue = columnValue;
        this.operatorOptions = operatorOptions;
        this.operatorValue = operatorValue;
        this.value = value;
        this.submittable = false;
    };

    var filterState = {
        data: []
    };

    var addFilter = function (id) {
        var updatingExisting = filterState.data[id].submittable;
        updateDataFromForm(id);
        var filter = buildFilterFromData();

        var filterString = filterId ? "/" + filterId : "";

        neon.util.AjaxUtils.doPostJSON(filter, neon.query.SERVER_URL + "/services/filterservice/updateFilter" + filterString,
            {
                success: function (uuid) {
                    filterId = uuid.addedIds[0];
                    if(!updatingExisting){
                        filterState.data.push(new FilterRow());
                    }
                    messageHandler.publishMessage(neon.eventing.Channels.FILTERS_CHANGED, {});
                    redrawTemplateFromData();
                }
            });
    };

    var removeFilter = function (id) {
        filterState.data.splice(id, 1);
        var filter = buildFilterFromData();

        var filterString = filterId ? "/" + filterId : "";
        neon.util.AjaxUtils.doPostJSON(filter, neon.query.SERVER_URL + "/services/filterservice/updateFilter" + filterString,
            {
                success: function (uuid) {
                    filterId = uuid.addedIds[0];
                    messageHandler.publishMessage(neon.eventing.Channels.FILTERS_CHANGED, {});
                    redrawTemplateFromData();
                }
            });
    };

    function buildCompoundWhereClause(data) {
        var whereClause;
        var clauses = [];
        var selected = $("input[type='radio'][name='boolean']:checked").val();

        $.each(data, function (index, filterData) {
            var clause = neon.query.where(filterData.columnValue, filterData.operatorValue, filterData.value);
            clauses.push(clause);
        });

        if (selected == "AND") {
            whereClause = neon.query.and.apply(this, clauses);
        }
        else {
            whereClause = neon.query.or.apply(this, clauses);
        }
        return whereClause;
    }

    function buildFilterFromData() {
        var dataset = neon.wizard.dataset();
        var baseFilter = new neon.query.Filter().selectFrom(dataset.database, dataset.table);

        var data = getSubmittableData();

        var whereClause;
        if(data.length === 0){
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

    var getSubmittableData = function () {
        var data = [];
        $.each(filterState.data, function (index, value) {
            if (value.submittable) {
                data.push(value);
            }
        });

        return data;
    };

    var updateDataFromForm = function (id) {
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

    var redrawTemplateFromData = function () {
        var source = $("#filters").html();
        var template = Handlebars.compile(source);
        var html = template(filterState);
        $('#filter-content').html(html);
    };

    var grid = function (columnNames) {
        columnOptions = columnNames;
        filterState.data = [];
        filterState.data.push(new FilterRow());
        redrawTemplateFromData();
    };

    return {
        addFilter: addFilter,
        removeFilter: removeFilter,
        grid: grid
    };
}();

$(function () {
    Handlebars.registerHelper('select', function (context, options) {
        var el = $('<select />').html(options.fn(this));
        el.find('option').filter(function () {
            return this.value === context;
        }).attr('selected', 'selected');
        return el.html();
    });
    Handlebars.registerHelper('escapeQuotes', function (context, options) {
        var el = $('<div/>').html(options.fn(this));
        if(context === ""){
            el.find('input').attr('value','""');
        }
        return el.html();
    });
});
