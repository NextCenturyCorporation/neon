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

// used for replacing a group of javascript files with a link to a single javascript file

// this uses the same syntax as grunt/yeoman's usemin task, but this doesn't actually do the concat/mininfication,
// rather it just replaces the block of files with the one listed in the comment block

// this allows different pages of an app to include only list a subset of files during development but still
// all use the same common library

class JSScriptReplacer {

    private static BLOCK_PATTERN = ~/(?s)<!--\s*build:[^\s\\]+\s*(.*?)\s*-->.*?<!--\s*endbuild\s*-->/

    public static enableJSResourceFiltering(project, warTask) {
        warTask.doFirst {
            def generatedWebAppDir = "${project.buildDir}/generated-web"
            new File(generatedWebAppDir).mkdirs()
            def originalWebAppDir = project.webAppDir
            project.webAppDirName = generatedWebAppDir

            ant.copy(todir: generatedWebAppDir) {
                fileset(dir: originalWebAppDir)
            }
            filterJSResources(generatedWebAppDir)
        }
    }

    private static def filterJSResources(dir) {
        new File(dir).eachFileRecurse { file ->
            if (!file.directory) {
                def name = file.name
                if (name.endsWith(".jsp") || name.endsWith(".html")) {
                    def text = file.getText()
                    if (text =~ BLOCK_PATTERN) {
                        file.write(replaceJSScriptBlocks(text))
                    }
                }
            }
        }
    }

    private static def replaceJSScriptBlocks(def text) {
        text.replaceAll(BLOCK_PATTERN, { "<script src=\"${it[1].trim()}\"></script>" })
    }

}