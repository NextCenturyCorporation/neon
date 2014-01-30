package com.ncc.neon.query.jackson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.TableQueryResult
import com.ncc.neon.query.mongo.MongoQueryResult
import com.ncc.neon.util.DateUtils
import org.bson.types.ObjectId
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test


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
