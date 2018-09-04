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

package com.ncc.neon.data

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.ElasticsearchException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ElasticSearchDataDeleter extends DefaultTask{
    // default value. build will override this
    String host = "elastic:10000"
    String databaseName = "integration-test"

    @TaskAction
    void run(){
        String[] connectionUrl = host.split(':', 2)
        String hostName = connectionUrl[0]
        int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 9300

        TransportClient client = ElasticSearchTransportConnector.connectViaTransport(hostName, port)
        deleteIndex(client)
    }

    /**
     * Delete the index.  Code is from:
     * http://programcreek.com/java-api-examples/index.php?api=org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
     */
    def deleteIndex(TransportClient client) {
        try {
            final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(databaseName)
            final DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(deleteIndexRequest).actionGet()
            if (!deleteIndexResponse.acknowledged) {
                println("Index " + databaseName + " not deleted")
            } else {
                println("Index " + databaseName + " deleted")
            }
        }
        catch (ElasticsearchException e) {
            // Depending on whether we're connecting to Elasticsearch 1 or 2, the exception has
            // different names. Rather than doing a bunch of conditional compilation stuff for this
            // case, just do an ugly name check
            def exceptionClass = e.getClass().getName()
            if (exceptionClass == "org.elasticsearch.index.IndexNotFoundException" || exceptionClass == "org.elasticsearch.indices.IndexMissingException") {
                // Do nothing here.  this just means that the index did not exist when we tried to delete it
                println("Index already deleted")
            } else
            {
                // The exception was apparently not about the index already being deleted, so let it
                // continue to unwind the stack
                throw e
            }
        }
    }
}
