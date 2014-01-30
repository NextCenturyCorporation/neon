package com.ncc.neon.language



/**
 * Thrown when parsing a text query into a Query object fails.
 */

class NeonParsingException extends RuntimeException{

    NeonParsingException(String message){
        super(message)
    }
}
