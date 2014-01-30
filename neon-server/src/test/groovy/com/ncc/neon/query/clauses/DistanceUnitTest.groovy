package com.ncc.neon.query.clauses

import org.junit.Test



class DistanceUnitTest {

    // these tests just make sure the distance units have not been incorrectly setup

    @Test
    void testConversions() {
        assert DistanceUnit.METER.meters == 1
        assert DistanceUnit.KM.meters == 1000
        assert DistanceUnit.MILE.meters == 1609.34
    }

}
