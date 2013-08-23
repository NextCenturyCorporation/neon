<!DOCTYPE html>
<html>
<head>
    <title>Timeline sandbox</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/timelinewidget.css">
    <link rel="stylesheet" type="text/css" href="css/barchart.css">
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">

    <script src="d3/d3.v3.min.js"></script>
    <script src="jquery/jquery-1.10.1.min.js"></script>
    <script src="jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="lodash/1.3.1/lodash.min.js"></script>
    <script src="namespaces.js"></script>
    <script src="barchart.js"></script>
    <script src="timeline.js"></script>

</head>
<body>
<button id="clear" onclick="clearing()">Clear</button> <button id="draw" onclick="draw()">Draw</button>
<br/>
<div id="timeline" class="timeline-div">
    <div id="chart" class="chart-div"></div>
</div>
<script>
    var data = [
        {"date": new Date(2013, 2, 4), "count": 2},
        {"date": new Date(2013, 4, 8), "count": 4},
        {"date": new Date(2013, 1, 1), "count": 7},
        {"date": new Date(2013, 1, 7), "count": 1}
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
        $("#chart").empty();
    }

</script>

</body>
</html>
