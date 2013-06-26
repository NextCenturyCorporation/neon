package com.ncc.neon.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
class DateUtilsTest {

    /** an arbitrary date to use for this test */
    private static final def DATE_NO_MILLIS = new DateTime(2012,4,15,16,30,0,DateTimeZone.UTC).toDate()
    private static final def DATE_WITH_MILLIS = new DateTime(2012,4,15,16,30,0,123,DateTimeZone.UTC).toDate()


    /** the ISO-8601 representation of {@link #DATE_NO_MILLIS} */
    private static final def ISO8601_DATE_STRING_NO_MILLIS = "2012-04-15T16:30:00Z"
    private static final def ISO8601_DATE_STRING_WITH_MILLIS = "2012-04-15T16:30:00.123Z"

    @Test
    void "create date from ISO8601 string no millis"() {
        def date = DateUtils.fromISO8601String(ISO8601_DATE_STRING_NO_MILLIS)
        assert date == DATE_NO_MILLIS
    }

    @Test
    void "create date from ISO8601 string with no timezone"() {
        def date = DateUtils.fromISO8601String("2012-04-15")
        assert date == new DateTime(2012,04,15,0,0,0,DateTimeZone.UTC).toDate()
    }

    @Test
    void "create date from ISO8601 string with non GMT timezone"() {
        def date = DateUtils.fromISO8601String("2012-04-15T16:30-04:00")
        assert date == new DateTime(2012,04,15,16,30,0,DateTimeZone.forOffsetHours(-4)).toDate()
    }



    @Test
    void "convert ISO8601 no millis string to date"() {
        def iso860String = DateUtils.toISO8601String(DATE_NO_MILLIS)
        assert iso860String == ISO8601_DATE_STRING_NO_MILLIS
    }

    @Test
    void "create date from ISO8601 string with millis"() {
        def date = DateUtils.fromISO8601String(ISO8601_DATE_STRING_WITH_MILLIS)
        assert date == DATE_WITH_MILLIS
    }

    @Test
    void "convert ISO8601 with millis string to date"() {
        def iso860String = DateUtils.toISO8601String(DATE_WITH_MILLIS)
        // the formatter still truncates the millis
        assert iso860String == ISO8601_DATE_STRING_NO_MILLIS
    }

}
