<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Timeline widget</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/barchartwidget.css">
    <link rel="stylesheet" type="text/css" href="css/barchart.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">
    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/charts.js -->
    <script src="d3/d3.v3.min.js"></script>
    <script src="jquery/jquery-1.10.1.min.js"></script>
    <script src="jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="lodash/1.3.1/lodash.min.js"></script>
    <script src="namespaces.js"></script>
    <script src="barchart.js"></script>
    <!-- endbuild -->

    <script src="js/chartwidget.js"></script>
    <script src="js/barchartwidget.js"></script>

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

    <script>
        $(document).ready(function() {
            $("#toggle").click(function () {
                $("#options").slideToggle("slow");

                if ($("#toggle-image").attr('src') == "img/arrow_down.png") {
                    $("#toggle-image").attr(
                            'src',
                            $("#toggle-image").attr('src').replace('_down', '_right')
                    );
                } else {
                    $("#toggle-image").attr(
                            'src',
                            $("#toggle-image").attr('src').replace('_right', '_down')
                    );
                }
            });
        });
    </script>

</head>
<body>
    <div class="chart-container">
        <div id="options-bar">
            <div id="toggle">
                <img id="toggle-image" src="img/arrow_down.png" />
            </div>

            <div id="options">
                <div class="controls-row">
                    <div class="control-group">
                        <label class="control-label" for="x">x-axis</label>

                        <div class="controls">
                            <select id="x" class="dropdown"></select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="y">y-axis</label>

                        <div class="controls">
                            <select id="y" class="dropdown"></select>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="chart" class="chart-div"></div>
    </div>
</body>
</html>
