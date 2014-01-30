package com.ncc.neon.connect

import groovy.transform.Canonical


/**
 * Contains connection information to a data source.
 */

@Canonical
class ConnectionInfo implements Serializable{

    private static final long serialVersionUID = 9017739423385857826L

    DataSources dataSource
    String connectionUrl

}
