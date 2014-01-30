package com.ncc.neon.transform



/**
 * Thrown when a transform name does not match any transformers in the TransformerRegistry
 */

class TransformerNotFoundException extends RuntimeException{

    TransformerNotFoundException(String message){
        super(message)
    }
}
