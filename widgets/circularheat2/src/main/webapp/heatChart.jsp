<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <link rel="stylesheet" href="css/circularHeatMap/style.css">
    <link rel="stylesheet" href="css/circularHeatMap/svg.css">
    <link rel="stylesheet" href="css/heatChart.css">
    <link rel="stylesheet" href="css/breadcrumbs.css">
    <link rel="stylesheet" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="css/bootstrap/bootstrap-theme.min.css">
    
    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/circularheat2.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-2.0.2.min.js"></script>
    <script src="js-lib/underscore/underscore-min.js"></script>
    <script src="js-lib/circularHeatChart/circularHeatChart.js"></script>
    <script src="javascript/heatChartTime.js"></script>
    <script src="javascript/heatChartConfig.js"></script>
    <script src="javascript/heatChartData.js"></script>
    <script src="javascript/heatChartNeonData.js"></script>
    <script src="javascript/heatChartWidget.js"></script>
    <script src="javascript/heatChartApp.js"></script>
    <!-- endbuild -->
 
    <script type="text/javascript">
        $(document).ready(function() {
        dataInject = new HeatChartNeonData();
        var heatChart = new HeatChartApp('year', new Date(2013, 0) ,dataInject);  
        // Simulate someone using a dropdown to select the date field
        // TODO: Do a real field drop-down
        window.setTimeout(function() {
            dataInject.setDateField('time');
        }, 2000);
    });
    </script>
  
</head>

<body>
    <nav class="navbar navbar-default navbar-static-top" role="navigation">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">Circular Heat Chart</a>
            <p id="baseDate" class="navbar-text pull-right"></p>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li id="baseDate" class="active"></li>
        </ul>
    </nav>
    <div class="row">
        <div class="col-xs-11 col-xs-offset-1">
            <ul id="breadcrumbs-two">
                <li><a id="year5Button" href="#">5-Year</a>
                </li>
                <li><a id="yearButton" href="#">Year</a>
                </li>
                <li><a id="monthButton" href="#">Month</a>
                </li>
                <li><a id="weekButton" href="#">Week</a>
                </li>
                <li><a id="dayButton" href="#">Day</a>
                </li>
                <li><a id="hourButton" href="#">Hour</a>
                </li>
                <li><a id="nowButton" href="#">Now</a>
                </li>
            </ul>
        </div>
    </div>
    <div>
        </br>
        </br>
    </div>
    <div class="row">
        <div class="col-xs-11 col-xs-offset-1">
            <div id="chart"></div>
        </div>
    </div>


</body>

</html>