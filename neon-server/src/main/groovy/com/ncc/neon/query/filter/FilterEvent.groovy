package com.ncc.neon.query.filter

import groovy.transform.ToString



/**
 * This class is sent to the client as JSON and so uses the raw String uuid,
 */
@ToString(includeNames = true)
class FilterEvent {

    String uuid
    DataSet dataSet

    static FilterEvent fromFilterKey(FilterKey filterKey){
        new FilterEvent(uuid: filterKey.uuid.toString(), dataSet: filterKey.dataSet)
    }

}
