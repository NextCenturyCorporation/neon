package com.ncc.neon.connect

import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.junit.Test

public class ElasticSearchRestConnectorTest {

    @Test
    public void connectViaRest() {
        RestClient rc = ElasticSearchRestConnector.connectViaRest("localhost", 9200)
        Response response = rc.performRequest("GET", "/_cat/indices")
        assert response.getStatusLine().statusCode == 200
    }
}
