package com.ncc.neon.query.filter

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext



@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class SelectionState implements Serializable{

    private static final long serialVersionUID = - 8135822897379806802L

    @Delegate
    FilterCache delegate = new FilterCache()

}
