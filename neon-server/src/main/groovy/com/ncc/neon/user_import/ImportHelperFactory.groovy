package com.ncc.neon.user_import

import com.ncc.neon.connect.NeonConnectionException
import org.springframework.stereotype.Component
import javax.annotation.Resource

@Component
class ImportHelperFactory {

    @Resource
    private ImportHelper mongoImportHelper


    ImportHelper getImportHelper(String databaseType) {
        switch (databaseType) {
            case "mongo":
                return mongoImportHelper
            default:
                throw new NeonConnectionException("Import of user-defined data is unsupported for database type ${databaseType}")
        }
    }

}
