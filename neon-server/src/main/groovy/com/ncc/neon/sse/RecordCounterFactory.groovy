package com.ncc.neon.sse

import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException

import org.springframework.stereotype.Component
import javax.annotation.Resource

@Component
class RecordCounterFactory {

    @Resource
    private MongoRecordCounter mongoRecordCounter

    RecordCounter getRecordCounter(String databaseType) {
        switch (databaseType as DataSources) {
            case DataSources.mongo:
                return mongoRecordCounter
            default:
                throw new NeonConnectionException("Import of user-defined data is unsupported for database type ${databaseType}")
        }
    }
}