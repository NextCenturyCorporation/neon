package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject


/**
 * Integration tests use json as their data format. This is an abstract class
 * that can be used to transform the json to a database specific format
 */
abstract class AbstractJSONGenerator {

    /** the number of spaces to use for indentation in the output json */
    private static final def INDENT_FACTOR = 4

    /**
     * Reads all json files (non-recursively) from the input directory, converts them to a form suitable for upload
     * to the specific database and writes them to the output directory. The output directory will be created if needed.
     * @param inputDirPath
     * @param outputDirPath
     * @param regex An optional regex to specify which fiels to process
     */
    void generateJson(inputDirPath, outputDirPath, regex = ~/.*\.json/) {
        new File(inputDirPath).eachFileMatch(regex) { file ->
            def text = file.text
            def json
            if (text.startsWith("[")) {
                json = new JSONArray(text)
                modifyJson(json)
            } else {
                json = new JSONObject(text)
                json.keys().each { key ->
                    def jsonArray = json.getJSONArray(key)
                    modifyJson(jsonArray)
                }
            }
            writeOutputFile(json, new File(outputDirPath), file.name)
        }
    }

    protected abstract void modifyJson(jsonArray)

    private static writeOutputFile(json, outDir, fileName) {
        outDir.mkdirs()
        def outfile = new File(outDir, fileName)
        outfile.withWriter { w ->
            w << json.toString(INDENT_FACTOR)
        }
    }


}
