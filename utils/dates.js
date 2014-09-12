/*Node.js script to modify dates in the data. Expects date field to be available in db.db_name["dig|snapshot.schema|dateCreated"]*/
/* run after unwind.js */
/*will only work on data sets of a certain size */


var host = "memex";
var port = 27017;
var db = "memex";
var collectionName = "mergedFixed";

var async = require('async');
var MongoClient = require('mongodb').MongoClient;

var winston = require('winston');
var log = new (winston.Logger)({ transports : [new (winston.transports.Console)({level:"info", timestamp: true})] });

MongoClient.connect("mongodb://" + host +":" + port + "/"+ db , function(err, db) {
	if(!err) {
		log.info("We are connected");
		var collection = db.collection(collectionName);
		fixDates(collection);
	}
});

var fixDates = function(collection) {
	log.info("Fetching rows");
	collection.find().toArray(function(err, items) {
		log.info(items.length);

		async.each(items, function(doc, docCallback) {
			log.info(doc["dig|snapshot"]["schema|dateCreated"]);
			doc["dig|snapshot"]["schema|dateCreated"] = new Date(doc["dig|snapshot"]["schema|dateCreated"]);
			collection.save(doc, {w:1}, function(err, savedDoc) {
				if(err) {
					log.error(err.message);
				}
				docCallback();
			});
		}, function() {
			process.exit();
		});
	});
};