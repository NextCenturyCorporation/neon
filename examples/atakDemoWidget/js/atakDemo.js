// Config items.
var ATAK_DATA_SERVER_URL = "localhost:12345";
var HOST_TYPE = "mongo";
var HOST = "localhost";
var DB = "test";
var TABLE = "alibaverstock130k";
var GEO_FILTER_KEY = "atakDemoGeoFilter";
var ENTITY_FILTER_KEY = "atakDemoEntityFilter";

// Set the neon server URL.
neon.SERVER_URL = "/neon";

/**
 * Event handler for filter changed events issued over Neon's messaging channels.
 * @param {Object} message A Neon filter changed message.
 * @method onAtakButton
 * @private
 */

// Wait for owf and neon to be ready, then attach our data handlers.
neon.ready(function () {
    var connection = new neon.query.Connection(); 
    // Create a messenger for Neon communication.
    var messenger = new neon.eventing.Messenger();
    // messenger.events({
    //     activeDatasetChanged: onDatasetChanged,
    //     filtersChanged: onFiltersChanged
    // });


    /**
     * Event handler for filter changed events issued over Neon's messaging channels.
     * @param {Object} message A Neon filter changed message.
     * @method onFiltersChanged
     * @private
     */
     // var onFiltersChanged = function() {

     // };

    /**
     * Event handler for filter changed events issued over Neon's messaging channels.
     * @param {Object} message A Neon filter changed message.
     * @method onFiltersChanged
     * @private
     */
    var onAtakButton = function() {
        console.log('Pushed the atak button');
        // Issues a neon data query and push the data to the atak server.
        var query = new neon.query.Query().selectFrom(TABLE).limit(10);

        //Execute the query and display the results.
        connection.executeQuery(query, function (result) {
            console.log(JSON.stringify(result.data));
        });
    };

    var broadcastActiveDataset = function () {
        // TODO: Alter or eliminate this when the Connection class in Neon is changed to emit
        // dataset selections.
        var message = {
            "datastore": HOST_TYPE,
            "hostname": HOST,
            "database": DB,
            "table": TABLE
        };
        messenger.publish(neon.eventing.channels.ACTIVE_DATASET_CHANGED, message);
    };

    var createFilterFromPoints = function(sw, ne) {
        // TODO
        return null;
    };

    var onBoundsMessage = function(sender, msg, channel) {

        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        var msgObj = JSON.parse(msg); // Convert the data payload to a JSON object

        console.log("atak demo widget creating a geo filter");
        var southWest = {
            latitude: parseFloat(msgObj.bounds.southWest.lat),
            longitude: parseFloat(msgObj.bounds.southWest.lon)
        };
        var northEast = {
            latitude: parseFloat(msgObj.bounds.northEast.lat),
            longitude: parseFloat(msgObj.bounds.northEast.lon)
        };

        var filter = createFilterFromPoints(southWest, northEast);

        messenger.replaceFilter(GEO_FILTER_KEY, filter, function () {
            console.log("Added a geo filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    };

    var createFilterFromEntity = function(entity) {
        // TODO
    };

    var onEntitySelection = function(sender, msg, channel) {
        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        var msgObj = JSON.parse(msg); // Convert the data payload to a JSON object

        messenger.replaceFilter(ENTITY_FILTER_KEY, filter, function () {
            console.log("Added an entity filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    }

    // Setup our data connection.
    connection.connect(HOST_TYPE, HOST);
    connection.use(DB);
    broadcastActiveDataset();

    // Listen for new bounds from map widgets.
    OWF.Eventing.subscribe("map.view.center.bounds", onBoundsMessage);

    // Listen for entity selections from Tangelo.
    OWF.Eventing.subscribe("tangelo.map.entity.selection", onEntitySelection);

    // Add the click handler.
    $('#atak-button').click(onAtakButton);
    
});