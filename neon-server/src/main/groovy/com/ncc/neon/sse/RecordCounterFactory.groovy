package com.ncc.neon.sse

import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException

import org.springframework.stereotype.Component
import javax.annotation.Resource

@Component
class RecordCounterFactory {

    @Resource
    private RecordCounter mongoRecordCounter

    @Resource
    private RecordCounter elasticSearchRecordCounter

    RecordCounter getRecordCounter(String databaseType) {
        switch (databaseType as DataSources) {
            case DataSources.mongo:
                return mongoRecordCounter
            case DataSources.elasticsearch:
                return elasticSearchRecordCounter
            default:
                throw new NeonConnectionException("There is no RecordCounter currently implemented for database type ${databaseType}")
        }
    }
}