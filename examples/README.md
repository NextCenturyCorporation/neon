To import the data into a MongoDB, run the following commands:

    unzip earthquakes.zip
    mongoimport --db test --collection earthquakes --type csv --headerline --stopOnError --file earthquakes.csv
    mongo test --eval "db.earthquakes.find().forEach(function(doc){doc.time = new ISODate(doc.time);db.earthquakes.save(doc)});"

The second line imports the CSV file, and the third line converts the time field from a string to a date.

This example data was collected from the USGS website at http://earthquake.usgs.gov/earthquakes/feed/v1.0/csv.php.
