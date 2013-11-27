package com.ncc.neon.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

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
