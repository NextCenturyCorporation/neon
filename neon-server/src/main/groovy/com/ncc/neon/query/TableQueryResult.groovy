package com.ncc.neon.query


/**
 * Query results of a tabular data store.
 * This is represented as a list of rows, where a row is a map of column names to values.
 */

class TableQueryResult implements QueryResult{


    List<Map<String, Object>> data = []

    public TableQueryResult(){

    }

    public TableQueryResult(List<Map<String, Object>> table){
        this.data = table
    }
}


