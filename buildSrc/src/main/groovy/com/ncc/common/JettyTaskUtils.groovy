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

package com.ncc.common

import org.gradle.api.plugins.jetty.internal.JettyPluginWebAppContext

class JettyTaskUtils {

    /**
     * Creates a jettyRun context that looks in the specified directories.
     * @param directories
     * @return The context for the jetty run task
     */
    static def createJettyRunContext(directories) {
        def context = new JettyPluginWebAppContext()
        setResources(context, getExistingDirectories(directories))
        return context
    }

    static void setResources(context, resources) {
        context.baseResource = createResourceCollection(resources)
    }

    static def createResourceCollection(resources) {
        return org.gradle.api.plugins.jetty.internal.JettyPluginWebAppContext.class.
                classLoader.loadClass("org.mortbay.resource.ResourceCollection").newInstance(resources)
    }

    static def getExistingDirectories(directories) {
        return directories.findResults {
            // projects may not have all directories if they don't need them, so only include the ones that exist
            new File(it).exists() ? it : null
        } as String[]
    }
}