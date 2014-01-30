package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject

/**
 * Used to generate a javascript file that configures the ports for javascript acceptance tests
 */
class AcceptanceTestJSHelperGenerator {

    static void generateJavascriptHelper(neonServerUrl, transformServiceUrl, outfile) {
        outfile.parentFile.mkdirs()
        outfile.withWriter { w ->
            w.println "var neonServerUrl = '${neonServerUrl}';"
            w.println "var transformServiceUrl = '${transformServiceUrl}';"
        }
    }

}
