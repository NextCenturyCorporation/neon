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
		moveToRoot(collection);
	}
});

var moveToRoot = function(collection) {
	log.info("Fetching rows");
	collection.find().toArray(function(err, items) {
		log.info(items.length);

		async.each(items, function(doc, docCallback) {
			if(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"] &&
				!Array.isArray(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]) &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|longitude"]) {

				log.info("Record:")
				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);
				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);
				log.info("")

				doc["phoneLatitude"] = parseFloat(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);
				doc["phoneLongitude"] = parseFloat(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|longitude"]);

				collection.save(doc, {w:1}, function(err, savedDoc) {
					if(err) {
						log.error(err.message);
					}
					docCallback();
				});
			} else {
				docCallback();
			}
		}, function() {
			process.exit();
		});
	});
};