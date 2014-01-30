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

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType



/**
 * A web service used during testing to make sure a request can be cancelled. This service just
 * sleeps for a while (enough time that the client code can cancel it)
 */
@Path('/timeouttest')
class TimeoutService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @SuppressWarnings('EmptyCatchBlock') // see comment in the catch block
    String timeout() {
        try {
            Thread.sleep(10000)
        }
        catch(InterruptedException e) {
            // ignore this. if the test finishes before this method actually finishes executing (even though the request
            // that invoked it was cancelled), this can throw an interruptedexception. in this case, we really
            // don't care
        }
        return "done"
    }
}
