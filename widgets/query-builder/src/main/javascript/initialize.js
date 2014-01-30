

$(function() {
    neon.query.SERVER_URL = $("#neon-server").val();

    function layout(){
        neon.queryBuilder.layoutResults();
    }

    $(window).resize(_.debounce(layout, 100));
    layout();
});