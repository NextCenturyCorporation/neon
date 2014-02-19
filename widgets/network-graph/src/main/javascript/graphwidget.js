/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */



/*
* This function demonstrates retrieving and displaying graph which has layout
* coordinates.
*/
neon.ready(function () {
    neon.query.SERVER_URL = $("#neon-server").val();

    var connectionId;
    var messenger = new neon.eventing.Messenger();

    messenger.registerForNeonEvents({
        activeConnectionChanged: onConnectionChanged
    });

    function onConnectionChanged(id){
        connectionId = id;
    }

    fetchData();

    function fetchData() {
        var query = new neon.query.Query().selectFrom('test', 'cpan');

        neon.query.executeQuery(connectionId, query, displayGraph);
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
