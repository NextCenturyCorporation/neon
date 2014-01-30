package com.ncc.neon.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat



/**
 * Converts dates to strings and visa-versa
 */
class DateUtils {

    private static final def DATE_PARSER = ISODateTimeFormat.dateTimeParser().withZoneUTC()
    private static final def DATE_FORMATTER = ISODateTimeFormat.dateTimeNoMillis()


    /**
     * Tries to parse the date string. If parsing fails, it returns the original text
     * @param dateString
     * @return Either the parsed date or the original string
     * @see #parseDate(java.lang.String)
     */
    static def tryToParseDate(String dateString) {
        try {
            return parseDate(dateString)
        }
        catch (DateParsingException) {
            return dateString
        }
    }

    /**
     * Converts a date from a string ISO-8601 format to a date object
     * @param iso8601DateString
     */
    @SuppressWarnings('CatchException')
    private static Date parseDate(String dateString) {
        try {
            return DATE_PARSER.parseDateTime(dateString).toDate()
        }
        catch (Exception e) {
            throw new DateParsingException(dateString, e)
        }
    }

    /**
     * Converts a date to an ISO-8601 formatted string
     * @param date
     */
    static String dateTimeToString(def date) {
        return DATE_FORMATTER.print(new DateTime(date).withZone(DateTimeZone.UTC))
    }

}
