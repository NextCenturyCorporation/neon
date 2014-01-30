package com.ncc.neon.data

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.util.JSON
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class MongoDataInserter extends DefaultTask{

    static final ALL_DATA_FILENAME = 'data.json'

    // default value. build will override this
    String host = "localhost"
    String databaseName = "concurrencytest"
    String tableName = "records"

    @TaskAction
    void run(){
        def db = new MongoClient(host).getDB(databaseName)
        def collection = db.getCollection(tableName)
        def dbList = parseJSON("/mongo-json/${ALL_DATA_FILENAME}")
        collection.insert(dbList)
        collection.ensureIndex(new BasicDBObject("location", "2dsphere"))
    }

    private static def parseJSON(resourcePath) {
        def testDataPath = "neon-server/src/test-data" + resourcePath
        return JSON.parse(new File(testDataPath).text)
    }

}
