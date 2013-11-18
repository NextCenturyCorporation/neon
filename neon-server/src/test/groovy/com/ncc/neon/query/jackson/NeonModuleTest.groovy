package com.ncc.neon.query.jackson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.TableQueryResult
import com.ncc.neon.query.mongo.MongoQueryResult
import com.ncc.neon.util.DateUtils
import org.bson.types.ObjectId
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

class NeonModuleTest {

    @Test
    void testObjectMappingSerialization(){
        def objectIdString = "51b0dc351cb440575f11c68a"

        ObjectMapperProvider provider = new ObjectMapperProvider()

        DBObject object = new BasicDBObject()
        object.put("_id", new ObjectId(objectIdString))
        object.put("date", new Date(1384807383787))
        MongoQueryResult result = new MongoQueryResult([object])

        String actual = provider.getContext(NeonModuleTest).writeValueAsString(result)
        assert actual == '{"data":[{"_id":"51b0dc351cb440575f11c68a","date":"2013-11-18T20:43:03Z"}]}'
    }

    @Test
    void testObjectMapping(){
        def objectIdString = "51b0dc351cb440575f11c68a"
        def objectId = new ObjectId(objectIdString)
        def date = new Date()

        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModuleTest)

        MongoQueryResult mongoResult = createMongoQueryResult(objectId, date)
        String serialize = mapper.writeValueAsString(mongoResult)
        TableQueryResult tqResult = mapper.readValue(serialize, TableQueryResult)

        assertTableQueryValues(tqResult, objectIdString, date)
    }

    private MongoQueryResult createMongoQueryResult(ObjectId objectId, Date date) {
        DBObject object = new BasicDBObject()
        object.put("_id", objectId)
        object.put("date", date)

        return new MongoQueryResult([object])
    }

    private void assertTableQueryValues(TableQueryResult tqResult, String objectIdString, Date date) {
        assert tqResult
        assert tqResult.data.size() == 1
        assert tqResult.data[0].get("_id") == objectIdString
        assert tqResult.data[0].get("date") == DateUtils.dateTimeToString(date)
    }
}
