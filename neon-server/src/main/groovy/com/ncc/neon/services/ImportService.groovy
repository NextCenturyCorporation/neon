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
import org.apache.commons.fileupload.MultipartStream
import org.apache.commons.fileupload.servlet.ServletFileUpload
import com.sun.jersey.multipart.MultiPart
import com.sun.jersey.multipart.BodyPart
import com.sun.jersey.multipart.FormDataParam

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

import groovy.json.JsonOutput

/**
 * Service for importing a file's contents into a database.
 */

@Component
@Path("/importservice")
class ImportService {

	@POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload/{host}/{databaseType}")
    public String getFile(@PathParam("host") String host,
                          @PathParam("databaseType") String databaseType,
                          @FormDataParam("file") InputStream dataInputStream) {

        /*
         * From Jersey. Uses a stream, so appears to work for even very large files. I tested by giving it a 363.4 mB file and it worked fine.
         * I obviously wasn't processing the whole thing, but I see no reason why it should fail if I do, given the use of a stream to get data.
         */
        byte[] buff = new byte[24]
        dataInputStream.read(buff)
        byte[] buff2 = new byte[24]
        dataInputStream.read(buff2)
        String s = new String(buff)
        s = s + new String(buff2)
        Map m = [data: s]

        /*
         * This tried to use the Apache fileupload library, but it didn't work - kept saying there was no reader for multipart form data.
        ByteArrayInputStream bStream = new ByteArrayInputStream(data.getBytes())
        @SuppressWarnings("deprecated")
        MultipartStream mStream = new MultipartStream(bStream, "---------------------------123206693813157131941135385742".toByteArray(), 1024, new MultipartStream.ProgressNotifier())
        Map m = [data: ServletFileUpload.isMultipartContent(data)]
        */
        return JsonOutput.toJson(m)
    }
}