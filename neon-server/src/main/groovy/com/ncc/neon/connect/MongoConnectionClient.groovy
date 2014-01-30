package com.ncc.neon.connect
import com.mongodb.MongoClient


/**
 * Holds a connection to mongo
 */

class MongoConnectionClient implements ConnectionClient{

    private MongoClient mongo

    public MongoConnectionClient(ConnectionInfo info){
        mongo = new MongoClient(info.connectionUrl)
    }

    MongoClient getMongo(){
        return mongo
    }

    /**
     * Close the connection to mongo.
     */
    @Override
    void close(){
        mongo?.close()
        mongo = null
    }

}
