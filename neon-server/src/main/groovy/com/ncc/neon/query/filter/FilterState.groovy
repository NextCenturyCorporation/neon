package com.ncc.neon.query.filter

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext


/**
 * Stores any filters applied to the datasets
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class FilterState implements Serializable{
    private static final long serialVersionUID = 7307506929923060807L

    @Delegate
    FilterCache delegate = new FilterCache()
}
