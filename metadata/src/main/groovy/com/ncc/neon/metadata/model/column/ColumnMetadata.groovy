package com.ncc.neon.metadata.model.column



/**
 * Contains metadata about a column in a dataset.
 */

interface ColumnMetadata {

    String getDatabaseName()
    String getTableName()
    String getColumnName()
    boolean isNumeric()
    boolean isTemporal()
    boolean isText()
    boolean isLogical()
    boolean isObject()
    boolean isArray()
    boolean isNullable()
    boolean isHeterogeneous()

}
