package com.ncc.neon.transform


/**
 * Holds all the transformer implementations. A transformer must be registered
 * so that it can be looked up by QueryExecutors.
 */

class TransformerRegistry {
    private final Map<String, Transformer> registry = [:]

    void register(Transformer transformer){
        registry.put(transformer.name, transformer)
    }

    Transformer getTransformer(String transformName){
        registry.get(transformName)
    }
}
