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
        xdata {
            logs {
                angular_example {
                    date {
                        value = "timestamp"
                    }
                    bar_x_axis {
                        value = "type"
                    }
                    y_axis {
                        value = "_id"
                    }
                    sort_by {
                        value = "timestamp"
                    }
                }
            }
        }
        memex {
            papers {
                angular_example {
                    date {
                        value = "date"
                    }
                    line_category {
                        value = "type"
                    }
                    bar_x_axis {
                        value = "year"
                    }
                    y_axis {
                        value = "_index"
                    }
                    color_by {
                        value = "magType"
                    }
                    size_by {
                        value = "mag"
                    }
                    sort_by {
                        value = "time"
                    }
                }
            }
            janes {
                angular_example {
                    date {
                        value = "date"
                    }
                    line_category {
                        value = "type"
                    }
                    bar_x_axis {
                        value = "Year"
                    }
                    y_axis {
                        value = "Value"
                    }
                    color_by {
                        value = "magType"
                    }
                    size_by {
                        value = "mag"
                    }
                    sort_by {
                        value = "time"
                    }
                }
            }
        }
        test {
            twitter36 {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    line_category {
                        value = "lang"
                    }
                    bar_x_axis {
                        value = "lang"
                    }
                    y_axis {
                        value = "_id"
                    }
                    color_by {
                        value = "lang"
                    }
                    size_by {
                        value = "lang"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    sort_by {
                        value = "created_at"
                    }
                    count_by { 
                        value = "screen_name"
                    }
                }
            }
            earthquakes {
                angular_example {
                    date {
                        value = "time"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_category {
                        value = "type"
                    }
                    bar_x_axis {
                        value = "net"
                    }
                    y_axis {
                        value = "mag"
                    }
                    color_by {
                        value = "magType"
                    }
                    size_by {
                        value = "mag"
                    }
                    sort_by {
                        value = "time"
                    }
                }
            }
            most_active {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_category {
                        value = "user_screen_name"
                    }
                    bar_x_axis {
                        value = "yyyy-mm"
                    }
                    y_axis {
                        value = "sentiment"
                    }
                    color_by {
                        value = "lang_primary"
                    }
                    size_by {
                        value = "foo"
                    }
                    sort_by {
                        value = "created_at"
                    }
                }
            }
            alibaverstock130k {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_category {
                        value = "country"
                    }
                    bar_x_axis {
                        value = "country"
                    }
                    y_axis {
                        value = "_id"
                    }
                    color_by {
                        value = "country"
                    }
                    size_by {
                        value = "foo"
                    }
                    sort_by {
                        value = "created_at"
                    }
                }
            }
            gbDate {
                angular_example {
                    date {
                        value = "created_at"
                    }
                    latitude {
                        value = "latitude"
                    }
                    longitude {
                        value = "longitude"
                    }
                    line_category {
                        value = "sentimentType"
                    }
                    bar_x_axis {
                        value = "sentimentType"
                    }
                    y_axis {
                        value = "sentiment"
                    }
                    color_by {
                        value = "sentimentType"
                    }
                    size_by {
                        value = "sentiment"
                    }
                    sort_by {
                        value = "created_at"
                    }
                }
            }
        }
    }
}
