var neon = neon || {};
neon.filter = function (){

    var columnOptions;
    var operatorOptions = [
        { "value": "eq", "text": "="},
        { "value": "ne", "text": "!="},
        { "value": "gt", "text": ">"},
        { "value": "lt", "text": "<"},
        { "value": "ge", "text": ">="},
        { "value": "le", "text": "<="}
    ];

    var CreateFilterData = function(columnValue, operatorValue, value){
        this.columnOptions = columnOptions;
        this.columnValue = columnValue;
        this.operatorOptions = operatorOptions;
        this.operatorValue = operatorValue;
        this.value = value;
        this.reachedServer = false;
    };

    var filterState = {
        data : []
    };

    var addFilter = function (id) {
        updateDataFromInput(id);
        filterState.data.push(new CreateFilterData());
        refresh();
    };

    var updateDataFromInput = function(id){
        var filterData = filterState.data[id];
        filterData.columnValue = $('#column-select-' + id +' option:selected').val();
        filterData.operatorValue = $('#operator-select-' + id +' option:selected').val();
        filterData.value = $('#value-input-' + id).val();
        //Just for client illustration
        filterData.reachedServer = true;
    }

    var removeFilter = function (id) {
        filterState.data.splice(id, 1);
        refresh();
    };

    var refresh = function() {
        var source = $("#filters").html();
        var template = Handlebars.compile(source);
        var html = template(filterState);

        $('#filter-content').html(html);
    };

    var grid = function(columnNames){
        columnOptions = columnNames;
        filterState.data = [];
        filterState.data.push(new CreateFilterData());
        refresh();
    };

    return {
        filterState : filterState,
        addFilter : addFilter,
        removeFilter : removeFilter,
        grid: grid,
        refresh : refresh
    };

}();

$(function(){
    Handlebars.registerHelper('select', function(context, options){
        var el = $('<select />').html(options.fn(this));
        el.find('[value=' + context + ']').attr({'selected':'selected'});
        return el.html();
    });

});
