/*
 * Copyright 2014 Next Century Corporation
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

package com.ncc.neon.mongo

import com.mongodb.DB
import com.mongodb.MongoClient
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.services.demo.MongoNeonHelper
import com.ncc.neon.services.demo.MongoTagCloudBuilder
import org.junit.AfterClass
import org.junit.Assume
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * Tests building a tag cloud with counts/frequencies of array values
 */
class MongoTagCloudBuilderTest {


    private static DB db

    private MongoTagCloudBuilder tagBuilder

    @BeforeClass
    static void beforeClass() {
        String host = System.getProperty("mongo.host")
        // Assume can't be used in beforeClass, so only set up the database if there is a host,
        // and then the before() method will skip if it needs to
        if (host != null && host != "") {
            db = new MongoClient(host).getDB("neonmongotagcloud")
        }
    }

    @AfterClass
    static void afterClass() {
        if (db != null) {
            db.mongo.close()
        }
    }

    @Before
    void before() {
        Assume.assumeTrue(db != null)
        tagBuilder = new MongoTagCloudBuilder()
        MongoNeonHelper neonHelper = new MongoNeonHelper()
        neonHelper.filterState = new FilterState()
        neonHelper.selectionState = new SelectionState()
        tagBuilder.mongoNeonHelper = neonHelper
    }


    @Test
    void "tag frequencies"() {
        Map<String,Integer> counts = tagBuilder.getTagCounts(db,"records","tags",3)

        assert counts.size() == 3

        // should be in descending order
        def iter = counts.entrySet().iterator()
        assertEntry(iter.next(),"tag2",3)
        assertEntry(iter.next(),"tag3",2)
        assertEntry(iter.next(),"tag1",1)
    }

    @Test
    void "tag frequencies with limit"() {
        Map<String,Integer> counts = tagBuilder.getTagCounts(db,"records","tags",2)

        assert counts.size() == 2

        // should be in descending order
        def iter = counts.entrySet().iterator()
        assertEntry(iter.next(),"tag2",3)
        assertEntry(iter.next(),"tag3",2)
    }


    private void assertEntry(def entry, String tag, int count) {
        assert entry.key == tag
        assert entry.value == count

    }

}

