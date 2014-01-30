package com.ncc.neon.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test


class DateUtilsTest {

    /** an arbitrary date to use for this test */
    private static final def DATE_NO_MILLIS = new DateTime(2012, 4, 15, 16, 30, 0, DateTimeZone.UTC).toDate()
    private static final def DATE_WITH_MILLIS = new DateTime(2012, 4, 15, 16, 30, 0, 123, DateTimeZone.UTC).toDate()

    /** the ISO-8601 representation of {@link #DATE_NO_MILLIS} */
    private static final def ISO8601_DATE_STRING_NO_MILLIS = "2012-04-15T16:30:00Z"
    private static final def ISO8601_DATE_STRING_WITH_MILLIS = "2012-04-15T16:30:00.123Z"

    @Test
    void "create date from ISO8601 string no millis"() {
        def date = DateUtils.parseDate(ISO8601_DATE_STRING_NO_MILLIS)
        assert date == DATE_NO_MILLIS
    }

    @Test
    void "create date from ISO8601 string with no timezone"() {
        def date = DateUtils.parseDate("2012-04-15")
        assert date == new DateTime(2012, 04, 15, 0, 0, 0, DateTimeZone.UTC).toDate()
    }

    @Test
    void "create date from ISO8601 string with non GMT timezone"() {
        def date = DateUtils.parseDate("2012-04-15T16:30-04:00")
        assert date == new DateTime(2012, 04, 15, 16, 30, 0, DateTimeZone.forOffsetHours(-4)).toDate()
    }

    @Test
    void "convert ISO8601 no millis string to date"() {
        def iso860String = DateUtils.dateTimeToString(DATE_NO_MILLIS)
        assert iso860String == ISO8601_DATE_STRING_NO_MILLIS
    }

    @Test
    void "create date from ISO8601 string with millis"() {
        def date = DateUtils.parseDate(ISO8601_DATE_STRING_WITH_MILLIS)
        assert date == DATE_WITH_MILLIS
    }

    @Test
    void "convert ISO8601 with millis string to date"() {
        def iso860String = DateUtils.dateTimeToString(DATE_WITH_MILLIS)
        // the formatter still truncates the millis
        assert iso860String == ISO8601_DATE_STRING_NO_MILLIS
    }

    @Test
    void "try to parse invalid date"() {
        def invalidDateString = "not a date"
        assert DateUtils.tryToParseDate(invalidDateString) == invalidDateString
    }

    @Test
    void "try to parse valid date"() {
        def date = DateUtils.tryToParseDate(ISO8601_DATE_STRING_WITH_MILLIS)
        assert date == DATE_WITH_MILLIS
    }

    @Test(expected = DateParsingException)
    void "invalid date throws parse exception"() {
        DateUtils.parseDate("invalidDateString")
    }

}
