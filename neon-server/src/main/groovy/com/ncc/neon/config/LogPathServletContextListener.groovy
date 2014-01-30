package com.ncc.neon.config

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener



/**
 * Listens for the app to initialize and sets the root path for the logs
 */
class LogPathServletContextListener implements ServletContextListener {

    @Override
    void contextInitialized(ServletContextEvent sce) {
        def context = sce.servletContext
        if (!System.getProperty("log.dir")) {
            System.setProperty("log.dir", "${context.getRealPath('/')}logs")
        }
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}
