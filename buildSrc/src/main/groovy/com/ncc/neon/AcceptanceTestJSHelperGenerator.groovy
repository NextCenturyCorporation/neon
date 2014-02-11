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

package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject

/**
 * Used to generate a javascript file that configures the ports for javascript acceptance tests
 */
class AcceptanceTestJSHelperGenerator {

    static void generateJavascriptHelper(neonServerUrl, transformServiceUrl, host, outfile) {
        outfile.parentFile.mkdirs()
        outfile.withWriter { w ->
            w.println "var neonServerUrl = '${neonServerUrl}';"
            w.println "var transformServiceUrl = '${transformServiceUrl}';"
            w.println "var host = '${host}';"
            w.println "var connectionId = 'mongo@${host}';"

        }
    }

}
