package com.ncc.neon.query



/**
 * Query results as a list of string values
 */

class ListQueryResult implements QueryResult{
    List<String> data = []

    ListQueryResult(){

    }

    ListQueryResult(Collection<String> list){
        data.addAll(list)
    }
}
