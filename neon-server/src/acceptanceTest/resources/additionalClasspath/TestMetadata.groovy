metadata {
    init {
        widget1 {
            initDataJson = '{"key1":"value1"}'
        }
    }
    columns {
        database1 {
            table1 {
                field1 {
                    numeric = true
                    logical = true
                    temporal = true
                    array = true
                    object = true
                    text = true
                    heterogeneous = true
                    nullable = true
                }
                field2 {
                    numeric = false
                    logical = false
                    temporal = false
                    array = false
                    object = false
                    text = false
                    heterogeneous = false
                    nullable = false
                }
            }
        }
    }
    widgets {
        database1 {
            table1 {
                widget1 {
                    aSelector {
                        value = "someValue"
                    }
                }
            }
        }
    }
}
