<!DOCTYPE html>
<html>
<head>
    <title>Timeline sandbox</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/barchart.css">
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">

    <!-- build:js js/charts.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js-lib/lodash/1.3.1/lodash.min.js"></script>
    <script src="js/toggle.js"></script>
    <script src="javascript/namespaces.js"></script>
    <script src="javascript/barchart.js"></script>
    <script src="javascript/timeline.js"></script>
    <!-- endbuild -->


</head>
<body>
<button id="clear" onclick="clearing()">Clear</button> <button id="draw" onclick="draw()">Draw</button>
<br/>
<div id="timeline" class="timeline-div">
    <div id="chart" class="chart-div"></div>
</div>
<script>
    var data = [

        {"date": new Date(2012, 2, 4), "count": 7},


        {"date": new Date(2011, 4, 11), "count": 7},
        {"date": new Date(2012, 4, 14), "count": 1},


    ];
    var opts = { "data": data,
        x: "date",
        y: "count",
        interval: charts.Timeline.MONTH,
        responsive: true
    };

    var timeline = new charts.Timeline('#chart', opts).draw();

    function draw(){
        timeline.draw();
    }

    function clearing(){
        timeline.draw();
    }

</script>

</body>
</html>
