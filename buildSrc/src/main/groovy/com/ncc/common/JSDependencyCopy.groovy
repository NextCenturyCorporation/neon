package com.ncc.common

/**
 * Utilities for copying the javascript dependencies of one project to another
 */
class JSDependencyCopy {

    // for each project in the list, copies the output of that project
    // and makes it a dependency of this project
    static void copyDependencies(project, jsDependencies) {
        jsDependencies.each { dep ->
            def depProj = project.findProject(":${dep}")
            def warTask = depProj.getTasksByName("war", true)

            // a task to copy all javascript and css from the dependent projects
            def copyJsTaskName = "copy${dep}JsDependencies"
            project.task([type: org.gradle.api.tasks.Copy], copyJsTaskName) {
                dependsOn warTask
                from(depProj.buildDir) {
                    include "*.js"
                }
                into project.jsDependenciesOutputDir
            }

            def copyCssTaskName = "copy${dep}CssDependencies"
            project.task([type: org.gradle.api.tasks.Copy], copyCssTaskName) {
                dependsOn warTask
                from("${depProj.webAppDir}/css") {
                    include "**/*"
                }
                into project.cssDependenciesOutputDir
            }

            project.processResources.dependsOn copyJsTaskName
            project.processResources.dependsOn copyCssTaskName

        }
    }

}

