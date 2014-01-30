package com.ncc.neon.metadata.model.widget

import groovy.transform.ToString


/**
 * Metadata about a widget to be retrieved on initialization.
 */

@ToString(includeNames = true)
class WidgetInitializationMetadata {
    String widgetName
    String initDataJson
}
