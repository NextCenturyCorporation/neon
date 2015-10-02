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

import org.junit.Before
import org.junit.Test

import org.json.JSONObject

import com.ncc.neon.connect.DataSources
import com.ncc.neon.userimport.ImportHelper
import com.ncc.neon.userimport.ImportHelperFactory
import com.ncc.neon.userimport.UserFieldDataBundle

import javax.ws.rs.core.Response

class ImportServiceTest {

    private ImportService importService

    private static final String HOST = "aHost"
    private static final String DATABASE_TYPE = DataSources.mongo.toString()
    private static final String UUID = "1234"

    @Before
    void setup() {
        importService = new ImportService()
        importService.importEnabled = "true"

        ImportHelper importHelper = [
            uploadFile: { host, userName, prettyName, fileType, stream ->
                return [jobID: "1234"]
            },
            checkTypeGuessStatus: { host, uuid ->
                return [complete: false, guesses: null, jobID: uuid]
            },
            loadAndConvertFields: { host, uuid, bundle ->
                return [jobID: uuid]
            },
            checkImportStatus: { host, uuid ->
                return new HashMap([complete: false, numCompleted: -1, failedFields: [], jobID: uuid])
            },
            dropDataset: { host, userName, prettyName ->
                if(host == HOST) {
                    return new HashMap([success: true])
                }
                return new HashMap([success: false])
            },
            doesDatabaseExist: { host, userName, prettyName ->
                return host != HOST
            }
        ] as ImportHelper
        importService.importHelperFactory = [getImportHelper: { dbType ->
            importHelper
        }] as ImportHelperFactory
    }

    @Test
    void "upload file success"() {
        Response response = importService.uploadFile(HOST, DATABASE_TYPE, "testUserName", "testPrettyName", "csv", new ByteArrayInputStream("data".getBytes()))
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        assert entity.getString("jobID") == UUID
    }

    @Test
    void "upload file failure"() {
        Response response = importService.uploadFile("wrongHost", DATABASE_TYPE, "testUserName", "testPrettyName", "csv", new ByteArrayInputStream("data".getBytes()))
        assert response.getStatus() == 406
    }

    @Test
    void "check type guesses status"() {
        Response response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, UUID)
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        assert !(entity.getBoolean("complete"))
        assert entity.isNull("guesses")
        assert entity.getString("jobID") == UUID
    }

    @Test
    void "load and convert fields"() {
        Response response = importService.loadAndConvertFields(HOST, DATABASE_TYPE, UUID, new UserFieldDataBundle())
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        assert entity.getString("jobID") == UUID
    }

    @Test
    void "check import status"() {
        Response response = importService.checkImportStatus(HOST, DATABASE_TYPE, UUID)
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        assert !(entity.getBoolean("complete"))
        assert entity.getInt("numCompleted") == -1
        assert entity.get("failedFields").length() == 0
        assert entity.getString("jobID") == UUID
    }

    @Test
    void "drop dataset success"() {
        Response response = importService.dropDataset(HOST, DATABASE_TYPE, "testUserName", "testPrettyName")
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        assert entity.getBoolean("success")
    }

    @Test
    void "drop dataset failure"() {
        Response response = importService.dropDataset("wrongHost", DATABASE_TYPE, "testUserName", "testPrettyName")
        assert response.getStatus() == 404
    }

    @Test
    void "test import not enabled"() {
        importService.importEnabled = "false"
        Response response = importService.uploadFile(HOST, DATABASE_TYPE, "testUserName", "testPrettyName", "csv", new ByteArrayInputStream("data".getBytes()))
        assert response.getStatus() == 403
        response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, UUID)
        assert response.getStatus() == 403
        response = importService.loadAndConvertFields(HOST, DATABASE_TYPE, UUID, new UserFieldDataBundle())
        assert response.getStatus() == 403
        response = importService.checkImportStatus(HOST, DATABASE_TYPE, UUID)
        assert response.getStatus() == 403
        response = importService.dropDataset(HOST, DATABASE_TYPE, "testUserName", "testPrettyName")
        assert response.getStatus() == 403
    }
}
