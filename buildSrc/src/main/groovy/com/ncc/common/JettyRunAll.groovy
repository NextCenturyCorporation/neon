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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.plugins.jetty.internal.Jetty6PluginServer
import org.gradle.logging.ProgressLoggerFactory

/**
 * A task to run multiple projects under the same embedded jetty server
 */
class JettyRunAll extends DefaultTask {

    /** the port to run on */
    int port

    /** the closure to get the directories to be set as path roots for jetty run. it takes one parameter, the project */
    def directoriesClosure

    /** if running in ssl mode. when true, the ssl parameters need to be supplied */
    boolean ssl = false
    String keystorePath
    String keystorePassword


    private def warProjects

    JettyRunAll() {
        description = "runs all of the specified projects in a single jetty instance"
        group = org.gradle.api.plugins.WarPlugin.WEB_APP_GROUP
        warProjects = findWarProjects()
        addDependencies()
    }

    def addDependencies() {
        warProjects.each { proj ->
            dependsOn proj.sourceSets.main.runtimeClasspath
        }
    }

    @TaskAction
    void run() {
        def server = new Jetty6PluginServer()
        ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader()
        // use the jetty class loader for loading jetty related classes
        Thread.currentThread().setContextClassLoader(server.proxiedObject.class.getClassLoader())
        try {
            runServer(server)
        }
        catch (Exception e) {
            throw new org.gradle.api.GradleException("Error running Jetty server.", e);
        }

        finally {
            Thread.currentThread().setContextClassLoader(originalClassloader)
        }
    }

    // note: methods in these classes are not private because of http://issues.gradle.org/browse/GRADLE-1439
    def getProgressLogger() {
        def progressLoggerFactory = getServices().get(ProgressLoggerFactory)
        return progressLoggerFactory.newOperation(JettyRunAll)
    }

    void logServerRunning(logger, contexts, connector) {
        def protocol = "http${ssl ? "s" : ""}"
        def message = "Running Jetty on ${protocol}://localhost:${connector.port}"
        logger.description = "${message} with contexts ${contexts}"
        logger.shortDescription = message
        logger.started()
    }

    def configureServer(server) {
        def connectors = []
        connectors << createConnector(server)
        server.connectors = connectors as Object[]
        server.configureHandlers()
        def contexts = []
        warProjects.each {
            def context = createContext(it)
            contexts << context.contextPath
            server.addWebApplication(context)
        }
        return contexts
    }

    def createConnector(server) {
        return ssl ? createSSLConnector() : server.createDefaultConnector(port)
    }

    def createSSLConnector() {
        def connector = Thread.currentThread().getContextClassLoader().loadClass("org.mortbay.jetty.security.SslSocketConnector").newInstance()
        connector.port = port
        connector.maxIdleTime = Jetty6PluginServer.DEFAULT_MAX_IDLE_TIME
        connector.keystore = keystorePath
        connector.keyPassword = keystorePassword
        return connector
    }

    def createContext(project) {
        def context = JettyTaskUtils.createJettyRunContext(directoriesClosure.call(project))
        def contextPath = project.war.baseName
        context.contextPath = "/${contextPath}"
        context.webXmlFile = new File(project.webAppDir, "WEB-INF/web.xml")
        context.classPathFiles = project.sourceSets.main.runtimeClasspath.files as List
        context.configure()
        return context
    }

    void runServer(server) {
        def contexts = configureServer(server)
        def logger = getProgressLogger()
        server.start()
        logServerRunning(logger, contexts, server.connectors[0])
        try {
            server.join()
        }
        finally {
            logger.completed()
        }
    }

    def findWarProjects() {
        return project.getAllprojects().findAll { it.getTasksByName("war", false) }
    }

}