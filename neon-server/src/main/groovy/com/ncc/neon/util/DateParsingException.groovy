package com.ncc.neon.util



/**
 * Used internally to determine if a string to date conversion was successful
 */
class DateParsingException extends RuntimeException {

    DateParsingException(String dateString, Throwable cause) {
        super("Unable to parse date ${dateString}", cause)
    }
}
