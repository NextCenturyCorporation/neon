package com.ncc.neon.services

import org.junit.Before
import org.junit.Test

/**
 *
 */
class InfoServiceTest {

    private InfoService service


    @Before
    void setup() {
        service = new InfoService()
        service.loadVersion()
    }

    @Test
    void testShowNeonVersion() {

        def today = Calendar.instance
        def year = today.get(Calendar.YEAR)

        String versionString = service.showNeonVersion()
        assert versionString.contains(year.toString())
    }

    @Test
    void "test Load Version"() {
        service.loadVersion()
    }
}
