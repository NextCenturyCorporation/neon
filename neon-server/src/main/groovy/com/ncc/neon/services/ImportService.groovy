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

    /**
     * Uploads data from a stream of a CSV spreadsheet into a database on the given host, and
     * returns a list of fields found in the spreadsheet with guesses as to their types.
     * @param host The host on which the data store to upload to is running.
     * @param databaseType The type of the data store to upload to.
     * @param user The name of the user with which to associate this data.
     * @param prettyName The "pretty" name of the database in which to put this data.
     * @param dataInputStream The stream containing the spreadsheet to read.
     * @return A map containing a new username if none was given, with which the new database is
     * associated, as well as a list of field names found in the spreadsheet and guesses
     * as to their types.
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload/{host}/{databaseType}")
    public Response uploadData(@PathParam("host") String host,
                               @PathParam("databaseType") String databaseType,
                               @FormDataParam("user") String user,
                               @FormDataParam("data") String prettyName,
                               @FormDataParam("file") InputStream dataInputStream) {
        LineIterator lineIter = IOUtils.lineIterator(dataInputStream, "UTF-8")
        String userName = user ?: UUID.randomUUID().toString()
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        List typeGuesses = helper.uploadData(host, userName, prettyName, lineIter)
        Map<String, String> toReturn = [types:typeGuesses, user: (user) ? '' : userName] // Only give a username back if we weren't given one.
        return Response.status(200).entity(JsonOutput.toJson(toReturn)).build()
    }

    /**
     * Drops a user-created database, given the host it's running on, the type of database it's running on,
     * and the user and pretty names associated with it.
     * @param host The host on which the data store to drop from is running.
     * @param databaseType The type of the data store the data to drop is in.
     * @param userName The username associated with the database to drop.
     * @param prettyName The "pretty" name of the database to drop.
     * @return A JSON formatted object with a success field that tells whether or not the drop succeeded.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("drop/{host}/{databaseType}")
    public String dropDataset(@PathParam("host") String host,
                              @PathParam("databaseType") String databaseType,
                              @QueryParam("user") String userName,
                              @QueryParam("data") String prettyName) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        return JsonOutput.toJson([success: helper.dropData(host, userName, prettyName)])
    }

    /**
     * Attempts to convert fields in a user-created database to types specified by the user, given the host
     * the databse is on, the host type, the database's user and prettified names, and a {@link UserFieldDataBundle} -
     * a bundle of data containing a date format string and a list of pairs of fields and types.
     * @param host The host on which to data store to convert in is running.
     * @param databaseType The type of data store the type of data to convert is in.
     * @param userName The username associated with the database storing the data to convert.
     * @param prettyName The "pretty" name of the database storing the data to convert.
     * @param data The bundle of data containing a user-given date format string and a list of fields and
     * the types they should be converted to.
     * @return A response containing a list of fields that failed to convert. Response code is 200 if no fields
     * failed and 418 otherwise.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("convert/{host}/{databaseType}")
    public Response convertFields(@PathParam("host") String host,
                                  @PathParam("databaseType") String databaseType,
                                  @QueryParam("user") String userName,
                                  @QueryParam("data") String prettyName,
                                  UserFieldDataBundle data) {
        ImportHelper helper = importHelperFactory.getImportHelper(databaseType)
        List failedFields = helper.convertFields(host, userName, prettyName, data)
        return Response.status((failedFields) ? 418 : 200).entity(JsonOutput.toJson(failedFields)).build()
    }
}