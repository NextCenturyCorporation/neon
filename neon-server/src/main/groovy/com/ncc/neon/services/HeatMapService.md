##heatmapservice/query/{host}/{databaseType}

| Param | type | description |
|-------|------|-------------|
| ignoreFilters | boolean | defaults to false |
| selectionOnly | boolean | defaults to false |
| ignoredFilterIds | Set<String> | |
| minLat | double | |
| minLon | double | |
| maxLat | double | |
| maxLon | double | |
| lonField | String | longitude field - only used for mongo |
| latField | String | latitude field - only used for mongo |
| locationField | String | geopoint mapped location field - only used for elasticsearch |
| gridCount | int | Controls granularity of map gridding for heatmap calculations. For Mongo based queries, this number indicates the number of horizontal and vertical grid cells within the bounding box defined by the min and max lat and lon. A bounding box with horizontal range from min lon 10 to max lon 20 and a grid count of 5 would indicate heatmap grid cells of 2 degrees latitude width. For elasticsearch, the pre-defined elasticsearch geo grid aggregation values are used. For elasticsearch grid granularity sizes refer to [the elasticsearch geoHashGrid page](1) |

[1]: https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-geohashgrid-aggregation.html#_cell_dimensions_at_the_equator