var neon = neon || {};
neon.wizard = (function () {
    function getDatasetInfo(){
        var selectedDatabase = $('#database-select option:selected');
        var selectedTable = $('#table-select option:selected');
        return { database: selectedDatabase.val(), table: selectedTable.val() };
    }

    function populateDropdown(selectorString, dataArray){
        var selectSelector = $(selectorString);
        selectSelector.find('option').remove();
        $.each(dataArray, function (index, value) {
            $('<option>').val(value).text(value).appendTo(selectSelector);
        });
    }

    return {
        populateDropdown: populateDropdown,
        getDataset : getDatasetInfo
    };
})();
