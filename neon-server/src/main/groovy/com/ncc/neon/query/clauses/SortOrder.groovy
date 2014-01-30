package com.ncc.neon.query.clauses

import com.ncc.neon.query.jackson.SortOrderDeserializer
import org.codehaus.jackson.map.annotate.JsonDeserialize



/**
 * Ascending or descending enumeration
 */

@JsonDeserialize(using = SortOrderDeserializer)
enum SortOrder {

    ASCENDING(1), DESCENDING(-1)

    def direction

    SortOrder(direction) {
        this.direction = direction
    }

    static SortOrder fromDirection(def direction) {
        SortOrder.values().find { direction == it.direction }
    }

}