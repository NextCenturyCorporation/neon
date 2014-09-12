/*  To Run:

	npm install async mongodb winston
	node completeDemoDateManipulation.js
*/


var host = "memex";
var port = 27017;
var db = "memex";
var collectionName = "testNewScript";

var stringDefault = "";
var numberDefault = "";
var dateDefault = null;


var async = require('async');
var MongoClient = require('mongodb').MongoClient;

var winston = require('winston');
var log = new (winston.Logger)({ transports : [new (winston.transports.Console)({level:"info", timestamp: true})] });

MongoClient.connect("mongodb://" + host +":" + port + "/"+ db , function(err, db) {
	if(!err) {
		log.info("We are connected");
		var coll = db.collection(collectionName);
		unwind(coll);
	}
});

var unwind = function(collection) {
	log.info("Fetching for dig|snapshot unwind process");
	var raCount = 0;
	collection.find().toArray(function(err, items) {
		log.info(items.length);

		async.each(items, function(doc, docCallback) {
			var isArray = Array.isArray(doc["dig|snapshot"]);
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
		}, function() {
			log.info("");
			theRest(collection);
		});
	});
}

var theRest = function(collection) {
	log.info("Fetching rows");
	collection.find().toArray(function(err, items) {
		log.info(items.length);

		async.each(items, function(doc, docCallback) {
			//age to root and parse
			if(doc["dig|snapshot"]["dig|mentionsPersonAge"]) {
				log.info(doc["dig|snapshot"]["dig|mentionsPersonAge"]);

				doc["age"] = parseFloat(doc["dig|snapshot"]["dig|mentionsPersonAge"]);
			} else {
				doc["age"] = numberDefault;
			}

			//airport code to root
			if(doc["dig|snapshot"] &&
				doc["dig|snapshot"]["dig|primaryLocation"] &&
				doc["dig|snapshot"]["dig|primaryLocation"]["schema|iataCode"]) {

				log.info(doc["dig|snapshot"]["dig|primaryLocation"]["schema|iataCode"]);

				doc["airport"] = doc["dig|snapshot"]["dig|primaryLocation"]["schema|iataCode"];
			} else {
				doc["airport"] = stringDefault;
			}

			//dates to ISODates
			if(doc["dig|snapshot"] &&
				doc["dig|snapshot"]["schema|dateCreated"]) {

				log.info(doc["dig|snapshot"]["schema|dateCreated"]);
				if(doc["dig|snapshot"]["schema|dateCreated"] == "1970-01-01T00:00:00") {
					doc["dig|snapshot"]["schema|dateCreated"] = null;
				} else {
					doc["dig|snapshot"]["schema|dateCreated"] = new Date(doc["dig|snapshot"]["schema|dateCreated"]);
				}
			}

			//drop at sign fields
			if(doc["@type"]) {
				delete doc["@type"];
			}
			if(doc["@id"]) {
				delete doc["@id"];
			}

			//name to root
			if(doc["dig|snapshot"]["dig|hasBodyPart"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"] &&
				!Array.isArray(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"]) &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"]["dig|pseudonym"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"]["dig|pseudonym"]["rdfs|label"]) {

				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"]["dig|pseudonym"]["rdfs|label"]);

				doc["name"] = doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPerson"]["dig|pseudonym"]["rdfs|label"];
			} else {
				doc["name"] = stringDefault;
			}

			//phone number location to root
			if(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"] &&
				!Array.isArray(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]) &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|longitude"]) {

				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);
				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);

				doc["phoneLatitude"] = parseFloat(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|latitude"]);
				doc["phoneLongitude"] = parseFloat(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["schema|location"]["schema|geo"]["schema|longitude"]);
			} else {
				doc["phoneLatitude"] = numberDefault;
				doc["phoneLongitude"] = numberDefault;
			}

			//phone number to root
			if(doc["dig|snapshot"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"] &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"] &&
				!Array.isArray(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]) &&
				doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["dig|tenDigitPhoneNumber"]) {

				log.info(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["dig|tenDigitPhoneNumber"]);

				doc["phoneNumber"] = parseFloat(doc["dig|snapshot"]["dig|hasBodyPart"]["dig|mentionsPhoneNumber"]["dig|tenDigitPhoneNumber"]);
			} else {
				doc["phoneNumber"] = numberDefault;
			}

			//primary location to root
			if(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"] &&
				!Array.isArray(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|latitude"]) &&
				!Array.isArray(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|longitude"])) {

				log.info(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|latitude"]);
				log.info(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|latitude"]);

				doc["primaryLatitude"] = parseFloat(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|latitude"]);
				doc["primaryLongitude"] = parseFloat(doc["dig|snapshot"]["dig|primaryLocation"]["schema|geo"]["schema|longitude"]);
			} else {
				doc["primaryLatitude"] = numberDefault;
				doc["primaryLongitude"] = numberDefault;
			}

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