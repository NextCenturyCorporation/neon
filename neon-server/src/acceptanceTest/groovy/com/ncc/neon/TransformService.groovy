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
