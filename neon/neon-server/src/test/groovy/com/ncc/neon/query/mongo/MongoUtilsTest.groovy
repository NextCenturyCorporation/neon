package com.ncc.neon.query.mongo

import org.bson.types.ObjectId
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
 */

class MongoUtilsTest {

    // valid IDs for the tests
    private static final ID1 = "01234567890123456789ABCD"
    private static final OID1 = [$oid: ID1]

    private static final ID2 = "01234567890123456789ABCE"
    private static final OID2 = [$oid: ID2]

    @Test
    void "convert single oid to ObjectId"() {
        def objId = MongoUtils.toObjectId(OID1)
        compareIds(ID1, objId)
    }

    @Test
    void "convert collection of oids to ObjectIds"() {
        def objIds = MongoUtils.oidsToObjectIds([OID1, OID2])
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

    @Test
    void "convert list to maps"() {
        def values = ["a", "b", "c"]
        def key = "aKey"

        def maps = MongoUtils.listToMaps(values,key)
        assert maps.size() == 3
        assertSingleMap(maps[0],key,"a")
        assertSingleMap(maps[1],key,"b")
        assertSingleMap(maps[2],key,"c")
    }

    /**
     * Asserts that the specified map contains the one key value pair
     * @param map
     * @param key
     * @param value
     */
    private static def assertSingleMap(map,key,value) {
        assert map.size() == 1
        assert map[key] == value
    }

    private static def compareIds(id1, id2) {
        // mongo converts to lower case, so compare case insensitive
        assert id1.toString().toLowerCase() == id2.toString().toLowerCase()
    }
}
