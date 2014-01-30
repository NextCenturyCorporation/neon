package com.ncc.neon.data

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import com.mchange.v2.c3p0.ComboPooledDataSource

import java.sql.Connection
import java.sql.Statement

/*
 * ************************************************************************
 * Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 */

class HiveDataInserter extends DefaultTask{
    private static final def FIELD_TYPES = [_id: "string", firstname: "string", lastname: "string", city: "string", state: "string", salary: "int", hiredate: "timestamp"]

    // default values. build will override these
    String host = "shark:10000"
    String hdfsUrl = "hdfs://shark:8020"

    String databaseName = "concurrencytest"
    String tableName = "records"

    @TaskAction
    void run(){
        Configuration conf = new Configuration()
        conf.set("fs.defaultFS", hdfsUrl)
        FileSystem fileSystem = FileSystem.get(conf)

        File testDataFile = getFile("/hive-csv/data.csv")
        File fieldsFile = getFile("/hive-csv/fields.csv")
        def destFolder = "${hdfsUrl}/tmp/neonconcurrencytest-${new Random().nextInt(Integer.MAX_VALUE)}/"
        def destFolderPath = new Path(destFolder)
        fileSystem.mkdirs(destFolderPath)
        copyTestDataFile(fileSystem, testDataFile, destFolder)

        def tableScript = createTableScript(fieldsFile, destFolder)

        def dataSource = new ComboPooledDataSource()
        Connection connection = createConnection(dataSource)
        execute(connection, "create database ${databaseName}")
        execute(connection, tableScript)
        connection.close()
        dataSource.close()
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

    private File getFile(resourcePath){
        def testDataPath = "neon-server/src/test-data" + resourcePath
        return new File(testDataPath)
    }


    private def createTableScript(fieldsFile, destFolder) {
        def fields = fieldsFile.text.split(",").collect { field ->
            // fields staring with _ need to be escaped, otherwise not extra characters necessary
            def escapeChar = field.startsWith("_") ? '`' : ""
            def fieldType = FIELD_TYPES[field]
            // this can happen if our test data changes but the mappings are not updated.
            // this gives a useful error message.
            if (!fieldType) {
                throw new Error("Missing field type for ${field}")
            }
            return "${escapeChar}${field}${escapeChar} ${fieldType}"
        }.join(",")

        def script = new StringBuilder()
        script.append("create external table ${databaseName}.${tableName} (")
        script.append(fields)
        script.append(") row format delimited fields terminated by ',' location '${destFolder}'")
        return script.toString()
    }

    private void copyTestDataFile(fileSystem, testDataFile, destFolder) {
        def src = new Path(testDataFile.absolutePath)
        def destName = "${destFolder}concurrencytest.txt"
        def dest = new Path(destName)
        fileSystem.copyFromLocalFile(src, dest)
    }

}
