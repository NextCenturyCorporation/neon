package com.ncc.neon.data

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.indices.IndexMissingException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ElasticSearchDataDeleter extends DefaultTask{
    // default value. build will override this
    String host = "elastic:10000"
    String databaseName = "integration-test"
    String tableName = "records"

    @TaskAction
    void run(){
        String[] connectionUrl = host.split(':', 2)
        String hostName = connectionUrl[0]
        int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 9300

        println(hostName);
        println(port);

        TransportClient client = connectViaTransport(hostName, port)
        deleteIndex(client)
    }

    private TransportClient connectViaTransport(String host, int port) {
        Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.ignore_cluster_name", true).put("client.transport.sniff", true).build();
        return new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, port));
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
                println("Index " + tableName + " not deleted")
            } else {
                println("Index " + tableName + " deleted")
            }
        }
        catch (IndexMissingException missing) {
            // Do nothing here.  this just means that the index did not exist when we tried to delete it
            println("Index already deleted")
        }
    }
}
