package com.ncc.neon.metadata.store

/*
 * ************************************************************************
 * Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
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
        builder.metaClass.newLine = { delegate.append(System.getProperty("line.separator")); return delegate }
        builder.metaClass.indent { depth.times { delegate.append("\t") }; return delegate }
        builder.metaClass.startBlock = { key, separator -> delegate.indent().append("${key} ${separator}").newLine(); depth++; return delegate }
        builder.metaClass.endBlock = { separator -> depth--; delegate.indent().append(separator).newLine(); return delegate }
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
        writeAsString(key,"'${value}'");
    }

    private void writeKeyValuePairs(String key, Boolean value) {
        writeAsString(key,value);
    }

    private void writeKeyValuePairs(String key, Integer value) {
        writeAsString(key,value);
    }

    private void writeKeyValuePairs(String key, Float value) {
        writeAsString(key,value);
    }

    private void writeKeyValuePairs(String key, Double value) {
        writeAsString(key,value);
    }

    private void writeAsString(String key, Object value) {
        builder.indent().append("${key} = ${value}").newLine()
    }

}
