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

import com.ncc.neon.user_import.UserFieldDataBundle
import com.ncc.neon.user_import.ImportHelper
import com.ncc.neon.user_import.ImportHelperFactory

import org.apache.commons.io.LineIterator
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.sun.jersey.multipart.FormDataParam

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import groovy.json.JsonOutput

/**
 * Service for importing a file's contents into a database.
 */

@Component
@Path("/importservice")
class ImportService {

    @Autowired
    ImportHelperFactory importHelperFactory

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload/{host}/{databaseType}")
    public String uploadData(@PathParam("host") String host,
                             @PathParam("databaseType") String databaseType,
                             @FormDataParam("file") InputStream dataInputStream) {
        LineIterator lineIter = IOUtils.lineIterator(dataInputStream, "UTF-8")
        String identifier = UUID.randomUUID().toString()
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        List typeGuesses = helper.uploadData(host, identifier, lineIter)
        Map<String, String> toReturn = [identifier:identifier, types:typeGuesses]
        return JsonOutput.toJson(toReturn)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop/{host}/{databaseType}/{identifier}")
    public String dropDataset(@PathParam("host") String host,
                              @PathParam("databaseType") String databaseType,
                              @PathParam("identifier")String identifier) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        return JsonOutput.toJson([success: helper.dropData(host, identifier)])
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("convert/{host}/{databaseType}/{identifier}")
    public Response convertFields(@PathParam("host") String host,
                                @PathParam("databaseType") String databaseType,
                                @PathParam("identifier") String identifier,
                                UserFieldDataBundle data) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        List failedFields = helper.convertFields(host, identifier, data)
        return Response.status((failedFields) ? 418 : 200).entity(JsonOutput.toJson(failedFields)).build()
    }
}