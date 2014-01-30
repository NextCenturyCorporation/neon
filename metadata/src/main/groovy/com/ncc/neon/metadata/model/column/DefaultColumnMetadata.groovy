package com.ncc.neon.metadata.model.column

import groovy.transform.ToString



/**
 * Default implementation of metadata about a column.
 */

@ToString(includeNames = true)
class DefaultColumnMetadata implements ColumnMetadata{

    String databaseName
    String tableName
    String columnName
    boolean numeric
    boolean temporal
    boolean text
    boolean logical
    boolean object
    boolean array
    boolean nullable
    boolean heterogeneous

}
