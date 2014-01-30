package com.ncc.neon.query.mongo

import com.mongodb.DBObject
import com.ncc.neon.query.TableQueryResult



class MongoQueryResult extends TableQueryResult{

    MongoQueryResult(Iterable<DBObject> results){
        results.each {DBObject object ->
            data << object.toMap()
        }
    }
}
