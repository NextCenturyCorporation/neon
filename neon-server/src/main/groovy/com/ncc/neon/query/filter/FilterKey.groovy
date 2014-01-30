package com.ncc.neon.query.filter

import groovy.transform.Canonical



@Canonical
class FilterKey implements Serializable{

    private static final long serialVersionUID = -5783657018410727352L
    UUID uuid
    DataSet dataSet
}
