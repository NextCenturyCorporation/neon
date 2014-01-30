package com.ncc.neon.metadata.model.dataset

import groovy.transform.ToString


/**
 * Contains metadata about the active data set.
 */

@ToString(includeNames = true)
class WidgetAndDatasetMetadata {
    String widgetName
    String databaseName
    String tableName
    String elementId
    String value
}
