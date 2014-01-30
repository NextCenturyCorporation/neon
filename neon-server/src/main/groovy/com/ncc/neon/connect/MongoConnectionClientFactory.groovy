package com.ncc.neon.connect



class MongoConnectionClientFactory implements ConnectionClientFactory{

    @Override
    ConnectionClient createConnectionClient(ConnectionInfo info) {
        return new MongoConnectionClient(info)
    }
}
