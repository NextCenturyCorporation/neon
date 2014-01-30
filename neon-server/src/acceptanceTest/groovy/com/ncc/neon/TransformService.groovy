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

import javax.ws.rs.*
import javax.ws.rs.core.MediaType



/**
 * A web service used during testing to transform json
 */
@Path('/transformtest')
class TransformService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    String transform(String inputJson, @QueryParam("replacethis") String replaceThis, @QueryParam("replacewith") String replaceWith) {
        def output = inputJson.toString().replaceAll(replaceThis,replaceWith)
        // if the input data is not an array, transform it to an array
        if ( !output.startsWith("[")) {
            output = "[" + output + "]"
        }

        return output
    }
}
