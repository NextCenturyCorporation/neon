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

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.BasicDBObject
import com.mongodb.DBObject

@Component("mongo")
class MongoProperty implements PropertyInterface {

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${propertiesDatabaseName}')
    String propertiesDatabaseName

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${mongoHost}')
    String mongoHost

    private final String propertiesCollectionName = "properties"

    @Autowired
    private ConnectionManager connectionManager

    public Map getProperty(String key) {
        MongoClient mongo = getClient()
        DB database = mongo.getDB(propertiesDatabaseName)
        DBCollection collection = database.getCollection(propertiesCollectionName)
        Map toReturn = [key: key, value: null]

        BasicDBObject query = new BasicDBObject()
        query.put("_id", key)

        DBCursor allResults = collection.find(query)
        if (allResults.hasNext()) {
            DBObject obj = allResults.next()
            toReturn.put("value", obj.get("value"))
        }

        return toReturn
    }

    public void setProperty(String key, String value) {
        MongoClient mongo = getClient()
        DB database = mongo.getDB(propertiesDatabaseName)
        DBCollection collection = database.getCollection(propertiesCollectionName)

        BasicDBObject query = new BasicDBObject()
        query.put("_id", key)

        BasicDBObject doc = new BasicDBObject()
        doc.put("_id", key)
        doc.put("value", value)

        if (collection.find(query)) {
            collection.update(query, doc)
        } else {
            collection.insert(doc)
        }
    }

    public void remove(String key) {
        MongoClient mongo = getClient()
        DB database = mongo.getDB(propertiesDatabaseName)
        DBCollection collection = database.getCollection(propertiesCollectionName)

        BasicDBObject query = new BasicDBObject()
        query.put("_id", key)
        collection.remove(query)
    }

    public Set<String> propertyNames() {
        MongoClient mongo = getClient()
        DB database = mongo.getDB(propertiesDatabaseName)
        DBCollection collection = database.getCollection(propertiesCollectionName)
        DBCursor allResults = collection.find()
        Set<String> toReturn = [] as Set
        while (allResults.hasNext()) {
            DBObject obj = allResults.next()
            toReturn.add(obj.get("_id"))
        }
        return toReturn
    }

    public void removeAll() {
        MongoClient mongo = getClient()
        DB database = mongo.getDB(propertiesDatabaseName)
        DBCollection collection = database.getCollection(propertiesCollectionName)
        collection.remove(new BasicDBObject())
    }

    private MongoClient getClient() {
        connectionManager.currentRequest = new ConnectionInfo(
            dataSource: DataSources.mongo,
            host: mongoHost ?: "localhost"
        )
        return connectionManager.connection.mongo
    }
}
