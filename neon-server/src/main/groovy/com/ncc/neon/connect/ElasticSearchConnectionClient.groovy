/*
 * Copyright 2015 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.connect

import org.elasticsearch.client.Client

/**
 * Holds a connection to elasticsearch
 */
class ElasticSearchConnectionClient implements ConnectionClient{
    Client client

    public ElasticSearchConnectionClient(ConnectionInfo info){
        String[] connectionUrl = info.host.split(':', 2)
        String host = connectionUrl[0]
        int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 9300
        client = ElasticSearchTransportConnector.connectViaTransport(host, port)
    }

    /**
     * Close the elasticsearch client connection.
     */
    @Override
    void close(){
        client?.close()
        client = null
    }

}
