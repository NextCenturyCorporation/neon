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
