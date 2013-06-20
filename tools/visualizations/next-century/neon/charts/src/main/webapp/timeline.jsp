<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Timeline</title>
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">

    <script src="js/charts.js"></script>

    <%--<script src="d3/d3.v3.min.js"></script>--%>
    <%--<script src="jquery/jquery-1.10.1.min.js"></script>--%>
    <%--<script src="jqueryui/jquery-ui-1.10.3.custom.min.js"></script>--%>
    <%--<script src="namespaces.js"></script>--%>
    <%--<script src="timeline.js"></script>--%>


</head>
<body>


<div id="timeline">
    <div id="chart"></div>

</div>

<script>

    // month
    var data = [
        {"date": new Date(2013,2,7), "events": 2},
        {"date": new Date(2013,3,1), "events": 4},
        {"date": new Date(2013,0,8), "events": 7},
        {"date": new Date(2013,1,1), "events": 1},
        {"date": new Date(2013,1,9), "events": 12},
        {"date": new Date(2013,4,1), "events": 9},
        {"date": new Date(2013,5,13), "events": 8}
    ];

    //day
    /*
    var data = [];
    for ( var m = 5; m <= 6; m++ ) {
        for ( var d = 1; d < 30; d++ ) {
            var date = new Date(2013,m,d);
            var num = Math.floor((Math.random()*10)+1);
            console.log('num: ' + num);
            for ( var i = 0; i < num; i++ ) {
            var val = Math.floor((Math.random()*50)+1);
            data.push({"date":date,"events":val});
            }
        }
    } */

    // year
    //    var data = [
    //        {"date": new Date('2008-01-03'), "count": 2},
    //        {"date": new Date('2009-01-03'), "count": 5},
    //        {"date": new Date('2010-01-03'), "count": 9},
    //        {"date": new Date('2011-01-03'), "count": 3},
    //        {"date": new Date('2012-01-03'), "count": 1}
    //            ];

    //var data = [];

    var opts = { "data" : data,  "x": "date", "y": "events", "interval" : charts.Timeline.MONTH, width: 1200};
//    var opts = { "data" : data, "x": "date", "interval": charts.Timeline.DAY, width: 1200 };
    var timeline = new charts.Timeline('#chart', opts);
    timeline.onFilter(function (startDate, endDate) {
        console.log('filter [' + startDate + ',' + endDate + ']');
    });
    timeline.draw();


</script>

</body>
</html>