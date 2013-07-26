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