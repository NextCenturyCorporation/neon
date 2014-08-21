// Config items.
var ATAK_DATA_SERVER_URL = "localhost:12345";
var HOST_TYPE = "mongo";
var HOST = "localhost";
var DB = "test";
//var TABLE = "alibaverstock130k";
var TABLE = "south_america_tweets";
var GEO_FILTER_KEY = "atakDemoGeoFilter";
var TIME_FILTER_KEY = "atakDemoTimeFilter";
var ENTITY_FILTER_KEY = "atakDemoEntityFilter";
var TIME_FIELD = "created_at";
var LONGITUDE_FIELD = "longitude";
var LATITUDE_FIELD = "latitude";
var USER_FIELD = "user_name";

// TODO:  Make configurable
var ATAK_REST_SERVICE_URL = "http://10.1.93.167:8080/AtakRestService/pointsofinterest/post"
// var ATAK_REST_SERVICE_URL = "http://localhost:9090/AtakRestService/pointsofinterest/post"
var MAX_NUMBER_ELEMENTS_TO_ATAK = 1000

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


    /**
     * Event handler for filter changed events issued over Neon's messaging channels.
     * @param {Object} message A Neon filter changed message.
     * @method onFiltersChanged
     * @private
     */
    var onFiltersChanged = function (message) {
        console.log("received filter change from timeline " + message);
    };

    /**
     * Event handler for the Send button to push data from Neon to an outside server.
     * @method onAtakButton
     * @private
     */
    var onAtakButton = function () {
        console.log('Pushed the atak button');
        // Issues a neon data query and push the data to the atak server.
        var query = new neon.query.Query().selectFrom(TABLE);

        //Execute the query and display the results.
        connection.executeQuery(query, function (result) {
            console.log("Num Results: " + result.data.length);
            if (!result || result.data < 1) {
                console.log("No results from query.  Returning");
                return;
            }
            console.log("First results: " + JSON.stringify(result.data[0]));

            // Limit to a small number of results
            if (result.data.length > MAX_NUMBER_ELEMENTS_TO_ATAK) {
                result.data = result.data.slice(0, MAX_NUMBER_ELEMENTS_TO_ATAK)
                console.log("Num Results after slicing: " + result.data.length);
            }

            $.ajax({
                type: "POST",
                url: ATAK_REST_SERVICE_URL,
                data: JSON.stringify(result),
                contentType: 'application/json'
            });
        });
    };

    /**
     * Helper function for broadcasting a neon event with the server to which this
     * OWF to ATAK adapter widget is connecting.
     * @method broadcastActiveDataset
     * @private
     */
    var broadcastActiveDataset = function () {
        // Broadcast the dataset we are using.
        var message = {
            "datastore": HOST_TYPE,
            "hostname": HOST,
            "database": DB,
            "table": TABLE
        };
        messenger.publish(neon.eventing.channels.ACTIVE_DATASET_CHANGED, message);
    };

    var createFilterFromPoints = function (sw, ne, databaseName, tableName) {
        if (sw && ne && databaseName && tableName) {
            var leftClause = neon.query.where(LONGITUDE_FIELD, ">=", sw.longitude);
            var rightClause = neon.query.where(LONGITUDE_FIELD, "<=", ne.longitude);
            var bottomClause = neon.query.where(LATITUDE_FIELD, ">=", sw.latitude);
            var topClause = neon.query.where(LATITUDE_FIELD, "<=", ne.latitude);
            var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);

            //Deal with different dateline crossing scenarios.
            if (sw.longitude < -180 && ne.longitude > 180) {
                filterClause = neon.query.and(topClause, bottomClause);
            }
            else if (sw.longitude < -180) {
                leftClause = neon.query.where(LONGITUDE_FIELD, ">=", sw.latitude + 360);
                var leftDateLine = neon.query.where(LONGITUDE_FIELD, "<=", 180);
                var rightDateLine = neon.query.where(LONGITUDE_FIELD, ">=", -180);
                var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
                filterClause = neon.query.and(topClause, bottomClause, datelineClause);
            }
            else if (ne.longitude > 180) {
                rightClause = neon.query.where(LONGITUDE_FIELD, "<=", ne.longitude - 360);
                var rightDateLine = neon.query.where(LONGITUDE_FIELD, ">=", -180);
                var leftDateLine = neon.query.where(LONGITUDE_FIELD, "<=", 180);
                var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
                filterClause = neon.query.and(topClause, bottomClause, datelineClause);
            }

            return new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
        }
    };

    /**
     * Event handler for receiving bounds via the CMWAPI map.view.center.bounds channel.
     * Messages are stringified JSON in form of a bounds with southwest and northeast points.
     * Example: {"bounds":{"southWest":{"lat":24.5,"lon":-124},"northEast":{"lat":50.5,"lon":-79}}}
     * @method onBoundsMessage
     * @private
     */
    var onBoundsMessage = function (sender, msg, channel) {

        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        var msgObj = (typeof msg === "string") ? JSON.parse(msg) : msg; // Convert the data payload to a JSON object

        console.log("atak demo widget creating a geo filter");
        var southWest = {
            latitude: parseFloat(msgObj.bounds.southWest.lat),
            longitude: parseFloat(msgObj.bounds.southWest.lon)
        };
        var northEast = {
            latitude: parseFloat(msgObj.bounds.northEast.lat),
            longitude: parseFloat(msgObj.bounds.northEast.lon)
        };

        var filter = createFilterFromPoints(southWest, northEast, DB, TABLE);

        messenger.replaceFilter(GEO_FILTER_KEY, filter, function () {
            console.log("Added a geo filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    };

    var createFilterFromTimes = function(minTime, maxTime, databaseName, tableName) {
        var minClause = neon.query.where(TIME_FIELD, ">=", minTime);
        var maxClause = neon.query.where(TIME_FIELD, "<=", maxTime);
        var filterClause = neon.query.and(minClause, maxClause);
        return new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
    }

    var onTimeMessage = function(sender, msg, channel) {
        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        var msgObj = (typeof msg === "string") ? JSON.parse(msg) : msg; // Convert the data payload to a JSON object

        console.log("atak demo widget creating a time filter");

        var minTime = new Date(msgObj.time.min);
        var maxTime = new Date(msgObj.time.max);

        var filter = createFilterFromTimes(minTime, maxTime, DB, TABLE);

        messenger.replaceFilter(TIME_FILTER_KEY, filter, function () {
            console.log("Added a time filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    }

    /**
     * Creates a filter select object that has a where clause that "or"s all of the entities together
     * @param entities {Array} an array of entity/user strings that records must have to pass the filter
     * @returns {Object} a neon select statement
     * @method createFilterFromEntities
     */
    var createFilterFromEntities = function (entities, databaseName, tableName) {
        var filterClauses = entities.map(function (entityName) {
            return neon.query.where(USER_FIELD, "=", entityName.trim());
        });
        var filterClause = filterClauses.length > 1 ? neon.query.or.apply(neon.query, filterClauses) : filterClauses[0];
        return new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
    };

    /**
     * Event handler for receiving bounds via the tangelo.map.entity.selection channel.
     * Messages are name strings or arrays of name strings.
     * @method onEntitySelection
     * @private
     */
    var onEntitySelection = function (sender, msg, channel) {
        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        //var msgObj = JSON.parse(msg); // Convert the data payload to a JSON object
        msg = (typeof msg === "string") ? [msg] : msg;
        var msgObj = msg.map(function(it) { return it.split(/\s+/); });
        msgObj = _.flatten(msgObj);

        // An empty entity means don't filter on entity
        if (msgObj.length === 0 || (msgObj.length === 1 && msgObj[0] === "")) {
            messenger.removeFilter(ENTITY_FILTER_KEY);
            return;
        }

        var filter = createFilterFromEntities(msgObj, DB, TABLE);
        messenger.replaceFilter(ENTITY_FILTER_KEY, filter, function () {
            console.log("Added an entity filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    }

    /**
     * Event handler for receiving bounds via the entity.selection channel from Tangelo Mentions.
     * Messages are objects with a user param containing a name array.
     * Example: {user: ["sally", "joe"]}
     * @method onCommunitySelection
     * @private
     */
    var onCommunitySelection = function (sender, msg, channel) {
        var senderObj = JSON.parse(sender);  // Convert the sender string to a JSON object
        var msgObj = (typeof msg === "string") ? JSON.parse(sender) : msg;
        msgObj = msgObj.user;

        var filter = createFilterFromEntities(msgObj, DB, TABLE);
        messenger.replaceFilter(ENTITY_FILTER_KEY, filter, function () {
            console.log("Added an entity filter.");
        }, function () {
            console.log("Error: Failed to create filter.");
        });
    }

    // register for Neon events.
    messenger.events({
        //activeDatasetChanged: onDatasetChanged,
        filtersChanged: onFiltersChanged
    });

    // Setup our data connection.
    connection.connect(HOST_TYPE, HOST);
    connection.use(DB);
    broadcastActiveDataset();

    // Clean up any old GEO or Name filters.
    messenger.removeFilter(GEO_FILTER_KEY, function () {
        console.log("ATAK Demo Widget: removed Neon geo filter");
    }, function () {
        console.log("ATAK Demo Widget: failed to remove Neon geo filter");
    });

    messenger.removeFilter(ENTITY_FILTER_KEY, function () {
        console.log("ATAK Demo Widget: removed Neon entity filter");
    }, function () {
        console.log("ATAK Demo Widget: failed to remove Neon entity filter");
    });

    // Listen for new bounds from map widgets.
    OWF.Eventing.subscribe("geomap.center.view.bounds", onBoundsMessage);

    // Listen for new time message from map widget
    OWF.Eventing.subscribe("geomap.time.bounds", onTimeMessage);

    // Listen for entity selections from Tangelo Mentions app.
    OWF.Eventing.subscribe("tangelo.map.entity.selection", onCommunitySelection);

    // Listen for an entity being manually entered in the Tangelo Geoplot
    OWF.Eventing.subscribe("geomap.entity.entered", onEntitySelection);
    // Listen for an entity selection from the Tangelo GeoPlot.
    OWF.Eventing.subscribe("entity.selection", onEntitySelection);

    OWF.Preferences.setUserPreference({
        namespace: 'neon.atakDemo.databaseInfo',
        name: 'connectionInfo',
        value: JSON.stringify({type: HOST_TYPE, host: HOST, database: DB, table: TABLE})
    });

    // Add the click handler.
    $('#atak-button').click(onAtakButton);

});