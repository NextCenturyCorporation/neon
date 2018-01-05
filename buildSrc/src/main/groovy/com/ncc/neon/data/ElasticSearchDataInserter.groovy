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

import com.opencsv.CSVIterator
import com.opencsv.CSVReader
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.client.IndicesAdminClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Insert data into ElasticSearch.  Unfortunately, this still uses the TransportClient because
 * the RestHighLevelClient cannot do this yet.
 *
 * TODO:  Replace TransportClient when RestHighLevelClient implements admin tasks, like mappings.
 */
class ElasticSearchDataInserter extends DefaultTask{
    // default values. build will override these
    String host = "elastic:10000"

    String databaseName = "integration-test"
    String tableName = "records"
    String scrollTableName = "many-records"

    String[] header

    // The cut off for scrolling in Elasticsearch is 10,000 so add twice that much so that we can
    // fetch beyond the limit
    static final int NUMBER_OF_SCROLL_RECORDS = 20000

    @TaskAction
    void run() {
        String[] connectionUrl = host.split(':', 2)
        String hostName = connectionUrl[0]
        int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 9300

        TransportClient client = ElasticSearchTransportConnector.connectViaTransport(hostName, port)

        createIndex(client);
        processCSV(client);
        generateScrollRecords(client)
    }

    private void createIndex(TransportClient client) {
        IndicesAdminClient indexClient = client.admin().indices();

        // Create the index
        IndicesExistsResponse exists = indexClient.exists(new IndicesExistsRequest(databaseName)).actionGet()
        if (exists.isExists()) {
            println("WARNING:  index " + databaseName + " exists, but should not")
        } else {
            CreateIndexResponse created = indexClient.create(new CreateIndexRequest(databaseName)).actionGet()
            if (!created.acknowledged) throw new RuntimeException("Could not create index!");
        }

        createCsvMapping(client)
        createScrollMapping(client)
    }

    private void createCsvMapping(TransportClient client) {
        client.admin().indices()
            .preparePutMapping(databaseName)
            .setType(tableName)
            .setSource(XContentFactory.jsonBuilder().prettyPrint()
            .startObject()
            .startObject(tableName)
            .startObject("properties")
            .startObject("firstname").field("type", "keyword").field("index", "not_analyzed").endObject()
            .startObject("lastname").field("type", "keyword").field("index", "not_analyzed").endObject()
            .startObject("city").field("type", "keyword").field("index", "not_analyzed").endObject()
            .startObject("state").field("type", "keyword").field("index", "not_analyzed").endObject()
            .startObject("salary").field("type", "long").field("index", "not_analyzed").endObject()
            .startObject("hiredate").field("type", "date").field("format", "date_optional_time||E MMM dd HH:mm:ss zzz yyyy").field("index", "not_analyzed").endObject()
            .startObject("tags").field("type", "text").field("index", "not_analyzed").endObject()
            .endObject()
            .endObject()
            .endObject())
            .execute().actionGet()
    }

    private File getFile(resourcePath){
        def testDataPath = "neon-server/src/test-data" + resourcePath
        return new File(project.rootDir, testDataPath)
    }

    private void loadHeader() {
        File headerFile = getFile('/elasticsearch-csv/fields.csv')

        CSVIterator iterator = new CSVIterator(new CSVReader(new BufferedReader(new FileReader(headerFile))))

        // First line is header info
        if (iterator.hasNext()) {
            header = iterator.next()
        } else {
            println("Error:  No header line")
        }
    }

    def processCSV(TransportClient client) {
        loadHeader()

        File f = getFile('/elasticsearch-csv/data.csv')
        if (!f.exists()) {
            println(" file does not exist ")
            return
        }

        CSVIterator iterator = new CSVIterator(new CSVReader(new BufferedReader(new FileReader(f))))

        BulkRequestBuilder bulkRequest = client.prepareBulk()

        while (iterator.hasNext()) {
            IndexRequestBuilder indexRequest = processLine(iterator.next(), client)
            bulkRequest.add(indexRequest)
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet()
        if (bulkResponse.hasFailures()) {
            println(" Bulk failures: ")
            bulkResponse.each { it ->
                if (it.isFailed()) {
                    println("\tfailure: " + it.getFailureMessage())
                }
            }
        }
    }

    def processLine(String[] line, client) {
        String id

        XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
        for (int ii = 0; ii < line.length; ii++) {
            if(line[ii] != 'N') {
                if (header[ii] == "latitude" || header[ii] == "longitude") {
                    builder.field(header[ii], Double.parseDouble(line[ii]))
                } else if (header[ii] == "salary") {
                    builder.field(header[ii], Integer.parseInt(line[ii]))
                } else if (header[ii] == "tags") {
                    def tags = line[ii].split(':')
                    builder.field(header[ii], tags)
                } else if (header[ii] == "_id") {
                    id = line[ii]
                } else if (header[ii] == "hiredate") {
                    DateTimeFormatter formatIn = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                    builder.field(header[ii], formatIn.withZoneUTC().parseDateTime(line[ii]).toDate())
                } else {
                    builder.field(header[ii], line[ii])
                }
            }
        }
        builder.endObject()

        IndexRequestBuilder indexRequest = client.prepareIndex(databaseName, tableName, id).setSource(builder)
        return indexRequest
    }

    private void createScrollMapping(TransportClient client) {
        client.admin().indices()
            .preparePutMapping(databaseName)
            .setType(scrollTableName)
            .setSource(XContentFactory.jsonBuilder().prettyPrint()
            .startObject()
            .startObject(scrollTableName)
            .startObject("properties")
            .startObject("value").field("type", "string").field("index", "not_analyzed").endObject()
            .endObject()
            .endObject()
            .endObject())
            .execute().actionGet()
    }

    private void generateScrollRecords(TransportClient client) {
        BulkRequestBuilder bulkRequest = client.prepareBulk()
        for (int i = 0; i < NUMBER_OF_SCROLL_RECORDS; ++i) {
            IndexRequestBuilder indexRequest = client.prepareIndex(databaseName, scrollTableName).setSource("value", i)
            bulkRequest.add(indexRequest)
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet()
        if (bulkResponse.hasFailures()) {
            println(" Bulk failures: ")
            bulkResponse.each { it ->
                if (it.isFailed()) {
                    println("\tfailure: " + it.getFailureMessage())
                }
            }
        }
    }
}
