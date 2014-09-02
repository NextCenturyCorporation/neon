package com.ncc.neon.services

import org.junit.Before
import org.junit.Test

/**
 *
 */
class VersionServiceTest {

    private VersionService service


    @Before
    void setup() {
        service = new VersionService()
    }



    void testGetNeonVersion() {

    }

    @Test
    void "test Load Version"() {
        service.loadVersion()
    }
}
