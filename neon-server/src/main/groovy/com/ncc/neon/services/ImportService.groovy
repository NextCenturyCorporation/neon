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

import com.ncc.neon.userimport.UserFieldDataBundle
import com.ncc.neon.userimport.ImportHelper
import com.ncc.neon.userimport.ImportHelperFactory

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

    // Uploads a file, storing it wholesale in a data store, and triggers an asynchronous method to attempt to
    // find out what types the fields of records in the file are. Returns a job ID associated with the file.
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload/{host}/{databaseType}")
    public Response uploadFile(@PathParam("host") String host,
                               @PathParam("databaseType") String databaseType,
                               @FormDataParam("user") String user,
                               @FormDataParam("data") String prettyName,
                               @FormDataParam("type") String fileType,
                               @FormDataParam("file") InputStream dataInputStream) {
        String userName = user ?: UUID.randomUUID().toString()
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map jobID = helper.uploadFile(host, userName, prettyName, fileType, dataInputStream)
        return Response.status(200).entity(JsonOutput.toJson(jobID)).build()
    }

    // Checks on the status of finding out what types the fields in records of a file are,
    // given the job ID associated with it.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("guesses/{host}/{databaseType}/{uuid}")
    public Response checkTypeGuessStatus(@PathParam("host") String host,
                                             @PathParam("databaseType") String databaseType,
                                             @PathParam("uuid") String jobUUID) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map guesses = helper.checkTypeGuessStatus(host, jobUUID)
        return Response.ok().entity(JsonOutput.toJson(guesses)).build()
    }

    // Given user-defined type alues for the fields of a file, triggers an asynchronous method that pulls that file out of storage and
    // parses through it to get records, creating a database for them and converting the fields of its records as it goes along.
    //Returns the job ID associated with the file.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("convert/{host}/{databaseType}/{uuid}")
    public Response loadAndConvertFields(@PathParam("host") String host,
                                         @PathParam("databaseType") String databaseType,
                                         @PathParam("uuid") String uuid,
                                         UserFieldDataBundle data) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map jobID = helper.loadAndConvertFields(host, uuid, data)
        return Response.ok(JsonOutput.toJson(jobID)).build()
    }

    // Checks on the status of parsing a file for records and moving them into a datastore, given the job ID associated with the file.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("progress/{host}/{databaseType}/{uuid}")
    public Response checkImportStatus(@PathParam("host") String host,
                                      @PathParam("databaseType") String databaseType,
                                      @PathParam("uuid") String uuid) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map importStatus = helper.checkImportStatus(host, uuid)
        return Response.ok().entity(JsonOutput.toJson(importStatus)).build()
    }

    // Drops a set of user-given data from a data store, given its associated username and "pretty", human-readable name.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop/{host}/{databaseType}")
    public Response dropDataset(@PathParam("host") String host,
                              @PathParam("databaseType") String databaseType,
                              @QueryParam("user") String userName,
                              @QueryParam("data") String prettyName) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map success = helper.dropDataset(host, userName, prettyName)
        return Response.ok().entity(JsonOutput.toJson(success)).build()
    }
}