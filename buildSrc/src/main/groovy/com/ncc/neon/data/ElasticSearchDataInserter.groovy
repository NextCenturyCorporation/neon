package com.ncc.neon.data

import com.opencsv.CSVIterator
import com.opencsv.CSVReader
import groovy.json.JsonOutput
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.client.IndicesAdminClient
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class ElasticSearchDataInserter extends DefaultTask{
    // default values. build will override these
    String host = "elastic:10000"

    String databaseName = "integration-test"
    String tableName = "records"

    String[] header

    @TaskAction
    void run() {
        String[] connectionUrl = host.split(':', 2)
        String hostName = connectionUrl[0]
        int port = connectionUrl.length == 2 ? Integer.parseInt(connectionUrl[1]) : 9300

        TransportClient client = connectViaTransport(hostName, port)

        createIndex(client);
        processCSV(client);
    }

    private TransportClient connectViaTransport(String host, int port) {
        Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.ignore_cluster_name", true).put("client.transport.sniff", true).build();
        return new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, port));
    }

    private void createIndex(TransportClient client) {
        IndicesAdminClient indexClient = client.admin().indices();

        // Create the index
        IndicesExistsResponse exists = indexClient.exists(new IndicesExistsRequest(databaseName)).actionGet()
        if (exists.isExists()) {
            println("WARNING:  index " + databaseName + " exists, but should not")
        } else {
            CreateIndexResponse created = indexClient.create(new CreateIndexRequest(databaseName)).actionGet()
            println("Index created: " + created.acknowledged)
            if (!created.acknowledged) throw new RuntimeException("Could not create index!");
        }

        client.admin().indices()
            .preparePutMapping(databaseName)
            .setType(tableName)
            .setSource(XContentFactory.jsonBuilder().prettyPrint()
            .startObject()
            .startObject(tableName)
            .startObject("properties")
            .startObject("_id").field("type", "string").field("index", "not_analyzed").endObject()
            .startObject("firstname").field("type", "string").field("index", "not_analyzed").endObject()
            .startObject("lastname").field("type", "string").field("index", "not_analyzed").endObject()
            .startObject("city").field("type", "string").field("index", "not_analyzed").endObject()
            .startObject("state").field("type", "string").field("index", "not_analyzed").endObject()
            .startObject("salary").field("type", "long").field("index", "not_analyzed").endObject()
            .startObject("hiredate").field("type", "date").field("format", "date_optional_time").field("index", "not_analyzed").endObject()
            .startObject("tags").field("type", "string").field("index", "not_analyzed").endObject()
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
            println("Header: " + header)
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

        println("finished with all csv, sending bulk execute")
        BulkResponse bulkResponse = bulkRequest.execute().actionGet()
        if (bulkResponse.hasFailures()) {
            println(" Bulk failures: ")
            bulkResponse.each { it ->
                println("\tfailure: " + JsonOutput.toJson(it))
            }
        }
        println("done with bulk execute")
    }

    @SuppressWarnings('Println')
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
                    builder.field(header[ii], line[ii])
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
}