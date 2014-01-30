package com.ncc.neon.query.filter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString



/**
 * A DataSet is a database name and table name pair.
 */
@ToString(includeNames = true)
@EqualsAndHashCode
class DataSet implements Serializable {

    private static final long serialVersionUID = 1300981992049008425L
    String databaseName
    String tableName

}
