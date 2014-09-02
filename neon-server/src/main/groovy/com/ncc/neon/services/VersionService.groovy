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

package com.ncc.neon.services

import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Service for executing queries against an arbitrary data store.
 */

@Component
@Path("/versionservice")
class VersionService {

    private static final VERSION_FILE_NAME = System.getProperty("version.file", "version.properties")

    def versionString = ""

    /**
     * Get a string representing the version
     * @return The version information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("version")
    String getNeonVersion() {
        return versionString
    }

    @SuppressWarnings("JavaIoPackageAccess") // metadata is loaded from a file on the classpath
    @PostConstruct
    void loadVersion() {
        URL url = getClass().getResource("/${VERSION_FILE_NAME}")
        if (url) {
            File file = new File(url.toURI())
            println ("Loading version information from ${file}")
            FileInputStream fis=  new FileInputStream(file)
            Properties p = new Properties()
            p.load(fis)
            versionString = p.getProperty("build.version")
            println ("Version string ${versionString}")
        } else {
            println ("No ${VERSION_FILE_NAME} file found on classpath")
        }
    }


}
