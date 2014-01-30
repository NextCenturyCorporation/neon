package com.ncc.neon.metadata

import com.mongodb.MongoClient



/**
 * Contains a connection to Mongo.
 */

class MetadataConnection {

    private final MongoClient client

    MetadataConnection(MongoClient client){
        this.client = client
        addShutdownHook{
            close()
        }
    }

    MetadataConnection(){
        this(new MongoClient())
    }

    MetadataConnection(String url){
        this(new MongoClient(url))
    }

    MongoClient getClient(){
        return this.client
    }

    void close(){
        client.close()
    }

}
