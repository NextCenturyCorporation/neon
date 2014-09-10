var host = "memex";
var port = 27017;
var db = "memex";
var collection = "mergedFixed";

var async = require('async');
var MongoClient = require('mongodb').MongoClient;

var winston = require('winston');
var log = new (winston.Logger)({ transports : [new (winston.transports.Console)({level:"info", timestamp: true})] });

MongoClient.connect("mongodb://" + host +":" + port + "/"+ db , function(err, db) {
	if(!err) {
		log.info("We are connected");
		var collection = db.collection("mergedFixed");
		unwind(collection);
	} else {
		log.error(err.message);
	}
});

var raCount = 0;

var unwind = function(collection) {
	log.info("Fetching rows");
	collection.find().toArray(function(err, items) {
		log.info(items.length);

		async.each(items, function(doc, docCallback) {
			var isArray = Array.isArray(doc["dig|snapshot"]);
			//log.info(isArray);
			//break the array
			if(isArray) {
				var newDoc;

				log.info(doc["dig|snapshot"].length);
				async.each(doc["dig|snapshot"], function(snapshot, snapshotCallback) {
					newDoc = JSON.parse(JSON.stringify(doc));
					delete newDoc["_id"];
					delete newDoc["dig|snapshot"];
					log.info(newDoc);

					newDoc["dig|snapshot"] = snapshot;
					collection.save(newDoc, {w:1}, function(err, savedDoc) {
						if(err) {
							log.error(err.message);
						}
						snapshotCallback(err);
					});
				}, function(err) {
					log.info("removing original doc " + ++raCount);
					collection.remove({_id: doc._id}, {w:1}, function(err) {
						if(err) {
							log.error(err.message);
						}
						docCallback();
					});
				});
			} else {
				docCallback();
			}
			//save new records
			//drop the original
		}, function() {
			process.exit();
		})
	});
};