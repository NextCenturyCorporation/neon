/*
 * Copyright 2016 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.ncc.neon.property

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.annotation.Autowired

import org.elasticsearch.client.Client
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchHit
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.MatchAllQueryBuilder
import org.elasticsearch.action.admin.indices.flush.FlushRequest

@Component("elasticsearch")
class ElasticSearchProperty implements PropertyInterface {

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${propertiesDatabaseName}')
    String propertiesDatabaseName

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${elasticSearchHost}')
    String elasticSearchHost

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${elasticSearchPort}')
    String elasticSearchPort

    private final String propertiesTypeName = "properties"

    @Autowired
    private ConnectionManager connectionManager

    public Map getProperty(String key) {
        Client client = getClient()
        Map toReturn = [key: key, value: null]

        if (doesDBExist(client)) {
            GetResponse resp = client.prepareGet(propertiesDatabaseName, propertiesTypeName, key).get()

            if (resp.isExists()) {
                String value = resp.getSource().get("value")
                toReturn.put("value", value)
            }
        }

        return toReturn
    }

    public void setProperty(String key, String value) {
        Client client = getClient()

        Map<String, String> json = [:]
        json.put("value", value)

        client.prepareIndex(propertiesDatabaseName, propertiesTypeName, key)
            .setSource(json)
            .get()

        client.admin().indices().flush(new FlushRequest(propertiesDatabaseName)).actionGet()
    }

    public void remove(String key) {
        Client client = getClient()
        client.prepareDelete(propertiesDatabaseName, propertiesTypeName, key).get()
        client.admin().indices().flush(new FlushRequest(propertiesDatabaseName)).actionGet()
    }

    public Set<String> propertyNames() {
        Client client = getClient()
        Set<String> toReturn = [] as Set

        if (doesDBExist(client)) {
            MatchAllQueryBuilder query = QueryBuilders.matchAllQuery()

            SearchResponse resp = client.prepareSearch(propertiesDatabaseName)
                .setTypes(propertiesTypeName)
                .setQuery(query)
                .execute()
                .actionGet()

            SearchHits hits = resp.getHits()
            for (SearchHit hit : hits.getHits()) {
                toReturn.add(hit.getId())
            }
        }

        return toReturn
    }

    public void removeAll() {
        Client client = getClient()

        if (doesDBExist(client)) {
            MatchAllQueryBuilder query = QueryBuilders.matchAllQuery()

            SearchResponse resp = client.prepareSearch(propertiesDatabaseName)
                .setTypes(propertiesTypeName)
                .setQuery(query)
                .execute()
                .actionGet()

            SearchHits hits = resp.getHits()
            for (SearchHit hit : hits.getHits()) {
                client.prepareDelete(propertiesDatabaseName, propertiesTypeName, hit.getId()).get()
            }
            client.admin().indices().flush(new FlushRequest(propertiesDatabaseName)).actionGet()
        }
    }

    private boolean doesDBExist(Client client) {
        IndicesExistsResponse resp = client.admin().indices()
            .exists(new IndicesExistsRequest(propertiesDatabaseName))
            .actionGet()

        return resp.isExists()
    }

    private Client getClient() {
        def host = elasticSearchHost ?: "localhost"
        def port = elasticSearchPort ?: 9300
        def connInfo = new ConnectionInfo(dataSource: DataSources.elasticsearch, host: (host + ":" + port))
        connectionManager.currentRequest = connInfo
        return connectionManager.connection.client
    }
}
