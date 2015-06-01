/*
 * Copyright 2013 Next Century Corporation
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

import java.sql.Connection;
import java.sql.DriverManager;


/**
 * Holds a connection to mongo
 */

class CalciteConnectionClient implements ConnectionClient{

	/** Calcite is different from other implementations in that Calcite creates a connection to a single
	 * database within a database server.  So The Calcite "Connection" object actually keeps track of multiple connections
	 * to the host, one for each database being used.
	 */
    private Map<String, Connection> conxnMap = new HashMap<String, Connection>();
	
	/** The url to the Calcite database which consists of a protocol defining the underlying database (e.g. jdbc) and
	 * then database specifics like the host or port specified in a way that the implementation specific connection
	 * will understand.
	 */
	private String url;
	
	/** The underlying database implementation that Calcite will talk to. */
	private String protocol;
	
	private static final List<String> acceptedProtocols = ["jdbc", "mongo"];

    public CalciteConnectionClient(ConnectionInfo info){
		url = info.host;
		
		// We expect "host" to actually be a URL with the protocol indicating the underlying type of database.
		// Expected protocols are jdbc and mongo
		int protocolIndex = url.indexOf(':');
		
		if (protocolIndex > 0) {
			protocol = url.substring(0, protocolIndex);
			if (!acceptedProtocols.contains(protocol)) {
				throw new IllegalArgumentException("Unknown database type: " + protocol);
			}
		}
		else {
			throw new IllegalArgumentException("Must specify database type as protocol in Calcite URL");
		}
				
    }

	/**
	 * Create a Calcite connection to the database specified on the host help in the connection info.
	 */
    Connection getCalciteConxn(String databaseName){
		Connection conxn = conxnMap[databaseName];
		if (!conxn) {
			if (protocol.equals("jdbc")) {
				conxn = connectJdbc(databaseName);
			}
			else if (protocol.equals("mongo")) {
				conxn = connectMongo(databaseName);
			}
			conxnMap[databaseName] = conxn;
		}
        return conxn;
    }

    /**
     * Close the connection to mongo.
     */
    @Override
    void close(){
		for ( conxn in conxnMap ) {
			conxn.value.close();
		}
        conxnMap.clear();
    }

	private Connection connectJdbc(String databaseName) {
		Properties props = new Properties();
        props.put("model",
        "inline:"
            + "{\n"
            + "  version: '1.0',\n"
            + "  defaultSchema: '" + databaseName + "',\n"
            + "  schemas: [\n"
            + "     {\n"
            + "       type: 'jdbc',\n"
            + "       name: '" + databaseName.replace("\"", "") + "',\n"
            + "       jdbcUrl: '" + url + "',\n"
            + "       jdbcCatalog: '" + databaseName.replace("\"", "") + "',\n"
            + "       jdbcSchema: null\n"
            + "     }\n"
            + "  ]\n"
            + "}");

		System.err.println(props["model"]);
        return DriverManager.getConnection("jdbc:calcite:", props);
	}
	
	private Connection connectMongo(String databaseName) {
		Properties props = new Properties();
		// TODO: Haven't figured out how to specify schema files for Mongo.  Hardcoded to eathquake data right now.
		props.put("model", CalciteConnectionClient.class.getResource("/mongo-earthquakes-model.json").getPath());
		return DriverManager.getConnection("jdbc:calcite:", props);
	}
}
