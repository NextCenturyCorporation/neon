/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.data

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import com.mchange.v2.c3p0.ComboPooledDataSource

import java.sql.Connection
import java.sql.Statement

class SparkSQLDataInserter extends DefaultTask{
    private static final def FIELD_TYPES = [_id: "string", firstname: "string", lastname: "string", city: "string", state: "string", salary: "int", hiredate: "timestamp", tags: "array<string>"]

    // default values. build will override these
    String host = "spark:10000"
    String hdfsUrl = "hdfs://spark:8020"

    String databaseName = "concurrencytest"
    String tableName = "records"

    @TaskAction
    void run(){
        Configuration conf = new Configuration()
        conf.set("fs.defaultFS", hdfsUrl)
        FileSystem fileSystem = FileSystem.get(new URI(hdfsUrl), conf, "root")

        File testDataFile = getFile("/spark-csv/data.csv")
        File fieldsFile = getFile("/spark-csv/fields.csv")
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
        return new File(project.rootDir, testDataPath)
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
        script.append(") row format delimited fields terminated by ',' collection items terminated by ':' location '${destFolder}'")
        return script.toString()
    }

    private void copyTestDataFile(fileSystem, testDataFile, destFolder) {
        def src = new Path(testDataFile.absolutePath)
        def destName = "${destFolder}concurrencytest.txt"
        def dest = new Path(destName)
        fileSystem.copyFromLocalFile(src, dest)
    }

}
