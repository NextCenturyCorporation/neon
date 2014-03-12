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
            def path = context.getRealPath('/') ?: ""
            if ( !path?.endsWith(File.separator)) {
                path += File.separator
            }
            System.setProperty("log.dir", "${path}logs")
        }
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }
}
