/*
 * Copyright 2014 Next Century Corporation
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
package com.ncc.neon.metadata.store

/**
 * Writes out arbitrary objects to a groovy config file in a way that is easy to read/edit
 *
 * Note: This does not support nested collections (e.g. a collection where another element is a collection), but it
 * does support nested maps. While this class does make an attempt to be a generic configuration parser, it
 * has only been tested with regard to the needs of the metadata parser.
 */
class ConfigWriter {

    private final StringBuilder builder = new StringBuilder()

    /** the current nesting depth, used for indentation */
    private int depth = 0

    /** any fields to exclude from writing */
    private final Set<String> excludes


    ConfigWriter(Set<String> excludes = [] as Set) {
        this.excludes = Collections.unmodifiableSet(excludes + (['class', 'metaClass'] as Set))
        addBuilderMetaMethods()
    }

    @SuppressWarnings("MethodSize") // this is just adding a couple of metamethods. it's pretty straightforward and I (jstorey) don't think it will make it much cleaner to split it up

    private void addBuilderMetaMethods() {
        builder.metaClass.newLine = {
            delegate.append(System.getProperty("line.separator"))
            return delegate
        }

        builder.metaClass.indent = {
            depth.times {
                delegate.append("\t")
            }
            return delegate
        }

        builder.metaClass.startBlock = { key, separator ->
            delegate.indent().append("${key} ${separator}").newLine()
            depth++
            return delegate
        }

        builder.metaClass.endBlock = { separator ->
            depth--
            delegate.indent().append(separator).newLine()
            return delegate
        }
    }


    public String writeConfig(String name, Map<String, Object> map) {
        write(name, map)
        String config = builder.toString()
        depth = 0
        builder.length = 0
        return config
    }

    private void write(String name, Map<String, Object> map) {
        writeKeyValuePairs(name, map)
    }

    private void write(Object val) {
        val.properties.each { prop, propVal ->
            if (!excludes.contains(prop)) {
                writeKeyValuePairs(prop, propVal)
            }
        }
    }

    private void writeKeyValuePairs(String key, Map<String, Object> value) {
        builder.startBlock(key, "{")
        value.each { k, v ->
            if (!excludes.contains(k)) {
                writeKeyValuePairs(k, v)
            }
        }
        builder.endBlock("}")
    }

    private void writeKeyValuePairs(String key, Object val) {
        builder.startBlock(key, "{")
        write(val)
        builder.endBlock("}")
    }

    private void writeKeyValuePairs(String key, String value) {
        writeAsString(key, "'${value}'")
    }

    private void writeKeyValuePairs(String key, Boolean value) {
        writeAsString(key, value)
    }

    private void writeKeyValuePairs(String key, Integer value) {
        writeAsString(key, value)
    }

    private void writeKeyValuePairs(String key, Float value) {
        writeAsString(key, value)
    }

    private void writeKeyValuePairs(String key, Double value) {
        writeAsString(key, value)
    }

    private void writeAsString(String key, Object value) {
        builder.indent().append("${key} = ${value}").newLine()
    }

}
