package com.ncc.neon.query.mongo

import org.bson.types.ObjectId
import org.junit.Test



class MongoUtilsTest {

    // valid IDs for the tests
    private static final ID1 = "01234567890123456789ABCD"
    private static final ID2 = "01234567890123456789ABCE"

    @Test
    void "convert single oid to ObjectId"() {
        def objId = MongoUtils.toObjectId(ID1)
        assert objId instanceof ObjectId
        compareIds(ID1, objId)
    }

    @Test
    void "convert collection of oids to ObjectIds"() {
        def objIds = MongoUtils.oidsToObjectIds([ID1, ID2])
        assert objIds[0] instanceof ObjectId
        assert objIds[1] instanceof ObjectId
        compareIds(ID1, objIds[0])
        compareIds(ID2, objIds[1])
    }

    @Test
    void "objectId does not need to be converted"() {
        def objId = new ObjectId(ID1)
        // this should be the original value since it does not need to be converted
        def converted = MongoUtils.toObjectId(objId)
        assert objId.is(converted)
    }

    @Test
    void "objectId collection does not need to be converted"() {
        def objIds = [new ObjectId(ID1), new ObjectId(ID2)]
        // this should contain the original values since they did not need to be converted
        def converted = MongoUtils.oidsToObjectIds(objIds)
        assert objIds[0].is(converted[0])
        assert objIds[1].is(converted[1])
    }

    private static def compareIds(id1, id2) {
        // mongo converts to lower case, so compare case insensitive
        assert id1.toString().toLowerCase() == id2.toString().toLowerCase()
    }
}
