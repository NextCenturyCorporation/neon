

/*
* This function demonstrates retrieving and displaying graph which has layout
* coordinates.
*/
neon.ready(function () {
    neon.query.SERVER_URL = $("#neon-server").val();

    fetchData();

    function fetchData() {
        var query = new neon.query.Query().selectFrom('test', 'cpan');

        neon.query.executeQuery(query, displayGraph);
    }

    function displayGraph(data) {
        // translate and scale gephi layout data to html positions
        // Note: the translation and scale values may need adjustment        
        var nodeArr = $.map(data.data[0].nodes, function(val, index) {

            function transAndScale(val) {
                return (val + 600)/2;
            }

            return {
                id: val.id,
                x: transAndScale(val.x),
                y: transAndScale(val.y),
                label: val.label,
                size: val.size
            };
        });

        // draw the graph
        var opts = {"xPos":"x","yPos":"y"};
        var graph = new graphs.Graph("#graph", opts);
        graph.draw(nodeArr, data.data[0].edges);       
    }
});
