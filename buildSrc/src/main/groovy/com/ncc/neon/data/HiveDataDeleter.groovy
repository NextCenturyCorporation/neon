package com.ncc.neon.data

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import com.mchange.v2.c3p0.ComboPooledDataSource

class HiveDataDeleter extends DefaultTask {

    // default value. build will override this
    String host = "shark:10000"
    String databaseName = "concurrencytest"
    String tableName = "records"

    private static final String driverName = "org.apache.hive.jdbc.HiveDriver"

    @TaskAction
    void run(){
        def dataSource = new ComboPooledDataSource()
        Connection connection = createConnection(dataSource)
        execute(connection, "drop table if exists ${databaseName}.${tableName}")
        execute(connection, "drop database if exists ${databaseName}")
        connection.close()

    }

    Connection createConnection(dataSource) {
        def driverName = "org.apache.hive.jdbc.HiveDriver"
        def databaseType = "hive2"

        dataSource.setDriverClass(driverName)
        dataSource.setJdbcUrl("jdbc:${databaseType}://${host}/")
        return dataSource.getConnection("","")
    }

    private synchronized void execute(connection, query){
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }
}