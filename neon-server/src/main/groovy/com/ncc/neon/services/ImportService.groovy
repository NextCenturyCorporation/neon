/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.services

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.multipart.MultipartFile
import org.apache.commons.fileupload.MultipartStream
import org.apache.commons.fileupload.servlet.ServletFileUpload
import com.sun.jersey.multipart.MultiPart
import com.sun.jersey.multipart.BodyPart

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

import groovy.json.JsonOutput

/**
 * Service for importing a file's contents into a database.
 */

@Component
@Path("/importservice")
class ImportService {

//    @Autowired
//    CommonsMultipartResolver resolver

	@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload/{host}/{databaseType}")
    public String getFile(@PathParam("host") String host,
                          @PathParam("databaseType") String databaseType,
                          @RequestParam("file") MultiPart data) {

        /* From Jersey. Worked for smaller files, failed for > ~10 mB.
        BodyPart part0 = data.getBodyParts().get(0)
        String s = part0.getEntityAs(String.class)
        //System.out.println(s)
        Map m = [data: s]
        return JsonOutput.toJson(m) */

        ByteArrayInputStream bStream = new ByteArrayInputStream(data.getBytes())
        @SuppressWarnings("deprecated")
        MultipartStream mStream = new MultipartStream(bStream, "---------------------------123206693813157131941135385742".toByteArray(), 1024, new MultipartStream.ProgressNotifier())
        Map m = [data: ServletFileUpload.isMultipartContent(data)]
        return JsonOutput.toJson(m)
    }
}