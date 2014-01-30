package com.ncc.neon.connect

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext


/**
 * Holds which data store the user is connected to in his or her session
 */

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class SessionConnection implements Serializable{

    private static final long serialVersionUID = 6978933557828783521L
    ConnectionInfo connectionInfo

}
