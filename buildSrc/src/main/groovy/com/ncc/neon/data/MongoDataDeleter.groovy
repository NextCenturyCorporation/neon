package com.ncc.neon.data

import com.mongodb.MongoClient
import com.mongodb.util.JSON
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class MongoDataDeleter extends DefaultTask {

    // default value. build will override this
    String host = "localhost"
    String databaseName = "concurrencytest"

    @TaskAction
    void run(){
        def db = new MongoClient(host).getDB(databaseName)
        db.dropDatabase()
    }
}
