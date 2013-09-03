var neon = neon || {};
neon.wizard = (function () {
    function getBaseDatasetInfo(){
        var selectedDatabase = $('#database-select option:selected');
        var selectedTable = $('#table-select option:selected');
        return { database: selectedDatabase.val(), table: selectedTable.val() };
    }

    return {
        dataset : getBaseDatasetInfo
    };
})();
