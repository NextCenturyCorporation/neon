package com.ncc.neon.connect

import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.junit.Test

public class ElasticSearchRestConnectorTest {

    @Test
    public void connectViaRest() {
        RestClient rc = ElasticSearchRestConnector.connectViaRest("localhost", 9200)
        Response response = rc.performRequest("GET", "/haiti_5/_search")
        assert response.getStatusLine().statusCode == 200
        // System.out.println(EntityUtils.toString(response.getEntity()))
    }
}