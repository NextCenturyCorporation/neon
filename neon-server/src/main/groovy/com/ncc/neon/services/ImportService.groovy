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
import org.springframework.beans.factory.annotation.Value
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

    // This value determines whether or not import may be used, and is determined to the value of importEnabled in
    // neon-server/src/main/resources/app.properties. If that property is set to false, ALL import functions will
    // return 403 Forbidden messages rather than actually doing anything.
    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${importEnabled}')
    String importPropertyValue

    boolean importEnabled = (Boolean)importPropertyValue

    @Autowired
    ImportHelperFactory importHelperFactory

    /**
     * Uploads a file, storing it wholesale in a data store, and triggers an asynchronous method to attempt to
     * find out what types the fields of records in the file are. Returns a job ID associated with the file.
     * @param host The host on which the database is running.
     * @param databaseType The type of database in which the data is stored.
     * @param user The username to be associated with the data in the file.
     * @param prettyName The "pretty", human-readable database name to be associated with the data in the file.
     * @param fileType The extension of the file, used to determine how it should be parsed later.
     * @param dataInputStream The input stream containing the contents of the file.
     * @return A JSON object containing the job ID associated with the uploaded data, to be used throughout the rest
     * of the upload process - for more information on its exact format, check the documentation of {@link ImportHelper}.
     */
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
        if(!importEnabled) {
            return Response.status(403).build()
        }
        String userName = user ?: UUID.randomUUID().toString()
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map jobID = helper.uploadFile(host, userName, prettyName, fileType, dataInputStream)
        return Response.ok().entity(JsonOutput.toJson(jobID)).build()
    }

    /**
     * Checks on the status of finding out what types the fields in records of a file are, given the job ID
     * associated with it.
     * @param host The host on which the database is running.
     * @param databaseType The type of database in which the data is stored.
     * @param uuid The job ID associated with the file whose records are being checked.
     * @return A JSON object containing a list of fields found in the file, as well as guesses as to their types - for
     * more information on its exact format, check the documentation of {@link ImportHelper}.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("guesses/{host}/{databaseType}/{uuid}")
    public Response checkTypeGuessStatus(@PathParam("host") String host,
                                             @PathParam("databaseType") String databaseType,
                                             @PathParam("uuid") String jobUUID) {
        if(!importEnabled) {
            return Response.status(403).build()
        }
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map guesses = helper.checkTypeGuessStatus(host, jobUUID)
        return Response.ok().entity(JsonOutput.toJson(guesses)).build()
    }

    /**
     * Given user-defined type values for the fields of a file, triggers an asynchronous method that pulls that file out of storage and
     * parses through it to get records, creating a database for them and converting the fields of its records as it goes along.
     * @param host The host on which the database is running.
     * @param databaseType The type of database in which the data is stored.
     * @param uuid The job ID associated with the data to be parsed and converted.
     * @param data A {@link UserFieldDataBundle} containing the user's decisions for what type of data each field is, as well as a date
     * format string to be used when attempting to convert fields to dates.
     * @return A JSON object giving returning the same job ID that was given - for more informationon its exact format, check the
     * documentation of {@link ImportHelper}.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("convert/{host}/{databaseType}/{uuid}")
    public Response loadAndConvertFields(@PathParam("host") String host,
                                         @PathParam("databaseType") String databaseType,
                                         @PathParam("uuid") String uuid,
                                         UserFieldDataBundle data) {
        if(!importEnabled) {
            return Response.status(403).build()
        }
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map jobID = helper.loadAndConvertFields(host, uuid, data)
        return Response.ok(JsonOutput.toJson(jobID)).build()
    }

    /**
     * Checks on the status of parsing a file for records and moving them into a datastore, given the job ID associated with the file.
     * @param host The host on which the database is running.
     * @param databaseType The type of database in which the data is stored.
     * @param uuid The job ID associated with the data whose import status to check.
     * @return A JSON object giving the status of the import - for more informationon its exact format, check the documentation
     * of {@link ImportHelper}.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("progress/{host}/{databaseType}/{uuid}")
    public Response checkImportStatus(@PathParam("host") String host,
                                      @PathParam("databaseType") String databaseType,
                                      @PathParam("uuid") String uuid) {
        if(!importEnabled) {
            return Response.status(403).build()
        }
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map importStatus = helper.checkImportStatus(host, uuid)
        return Response.ok().entity(JsonOutput.toJson(importStatus)).build()
    }

    /**
     * Drops a set of user-given data from a data store, given its associated username and "pretty", human-readable name.
     * @param host The host on which the database is running.
     * @param databaseType The type of database in which the data is stored.
     * @param userName The username associated with the data to drop.
     * @param prettyName The "pretty", human-readable name associated with the data to drop.
     * @return A JSON object giving the status of the drop - for more information on its exact format, check the documentation
     * of {@link ImportHelper}.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop/{host}/{databaseType}")
    public Response dropDataset(@PathParam("host") String host,
                              @PathParam("databaseType") String databaseType,
                              @QueryParam("user") String userName,
                              @QueryParam("data") String prettyName) {
        if(!importEnabled) {
            return Response.status(403).build()
        }
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        Map success = helper.dropDataset(host, userName, prettyName)
        return Response.ok().entity(JsonOutput.toJson(success)).build()
    }
}