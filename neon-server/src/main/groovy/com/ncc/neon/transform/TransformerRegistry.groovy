/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
