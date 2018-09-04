/*
 * Copyright 2016 Next Century Corporation
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

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

/**
 * Create a Rest Client connection to ElasticSearch 5+
*/
class ElasticSearchRestConnector {
    public static RestHighLevelClient connectViaRest(String host, int port) {

        // TODO:  Determine what settings would be best for the Rest Client.  These are from the Transport
        // Settings settings = Settings.settingsBuilder().
        //     put("client.transport.ignore_cluster_name", true).
        //     put("client.transport.sniff", true).build()

        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port)))
    }
}


