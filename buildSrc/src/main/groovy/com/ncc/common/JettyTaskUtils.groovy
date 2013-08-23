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