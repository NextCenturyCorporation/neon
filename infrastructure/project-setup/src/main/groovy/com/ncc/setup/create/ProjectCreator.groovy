package com.ncc.setup.create

import com.ncc.setup.model.Project

/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

class ProjectCreator {

    private Project project

    public ProjectCreator(Project project){
        this.project = project
    }

    void createProject(){
        if(!project.projectName){
            throw new IllegalArgumentException("Project must have a name")
        }

        createProjectFolder()
        createBuild()
        createGroovy()
        createJava()
    }

    private void createProjectFolder() {
        new File(project.projectName).mkdir()
    }

    private void createBuild(){
        File file = new File("./$project.projectName/build.gradle")
        file.text = createPluginsString()
    }

    private String createPluginsString() {
        String plugins = ""
        if (project.groovyCode) {
            plugins = "apply from: 'gradle/groovy.gradle'"
            if(project.javaCode){
                plugins += "\napply plugin: 'checkstyle'\n" + "apply plugin: 'findbugs'\n"
            }
        }
        else if (project.javaCode) {
            plugins = "apply from: 'gradle/java.gradle'"
        }
        plugins
    }

    private void createGroovy(){
        if(!project.groovyCode){
            return
        }
        copyCodenarc()
        copyGroovyBuildDependencies()

        String packageAsPath = project.rootPackageName.replaceAll("\\.","/")
        createHelloGroovyFile(packageAsPath)
        createHelloGroovyTestFile(packageAsPath)
    }

    private void copyCodenarc(){
        String codenarcContent = getClass().getClassLoader().getResource("gradle-config/config/codenarc/codenarc.xml").text

        String codenarcPath = "./${project.projectName}/config/codenarc"
        new File(codenarcPath).mkdirs()
        new File("${codenarcPath}/codenarc.xml").text = codenarcContent
    }

    private void copyGroovyBuildDependencies() {
        String gradlePath = "./${project.projectName}/gradle"
        new File(gradlePath).mkdirs()
        new File("$gradlePath/groovy.gradle").text = getClass().getClassLoader().getResource("gradle-config/groovy.gradle").text
        new File("$gradlePath/codenarc.gradle").text = getClass().getClassLoader().getResource("gradle-config/codenarc.gradle").text
        new File("$gradlePath/jdepend.gradle").text = getClass().getClassLoader().getResource("gradle-config/jdepend.gradle").text
        new File("$gradlePath/docs.gradle").text = getClass().getClassLoader().getResource("gradle-config/docs.gradle").text
    }

    private void createHelloGroovyFile(String packageAsPath) {
        String fullMainPath = "./${project.projectName}/src/main/groovy/${packageAsPath}"
        new File(fullMainPath).mkdirs()
        File helloWorld = new File("${fullMainPath}/HelloGroovy.groovy")
        String packageLine = "package ${project.rootPackageName}\n"
        String fileContent = packageLine + getClass().getClassLoader().getResource("HelloGroovy.txt").text
        helloWorld.text = fileContent
    }

    private void createHelloGroovyTestFile(String packageAsPath) {
        String fullTestPath = "./${project.projectName}/src/test/groovy/${packageAsPath}"
        new File(fullTestPath).mkdirs()
        File helloWorldTest = new File("${fullTestPath}/HelloGroovyTest.groovy")
        String packageLine = "package ${project.rootPackageName}\n"
        String fileContent = packageLine + getClass().getClassLoader().getResource("HelloGroovyTest.txt").text
        helloWorldTest.text = fileContent
    }

    private void createJava(){
        if(!project.javaCode){
            return
        }

        copyCheckstyle()
        copyJavaBuildDependencies()

        String packageAsPath = project.rootPackageName.replaceAll("\\.","/")
        createHelloJavaFile(packageAsPath)
        createHelloJavaTestFile(packageAsPath)
    }

    private void copyCheckstyle(){
        String checkstyleContent = getClass().getClassLoader().getResource("gradle-config/config/checkstyle/checkstyle.xml").text

        String checkstylePath = "./${project.projectName}/config/checkstyle"
        new File(checkstylePath).mkdirs()
        new File("${checkstylePath}/checkstyle.xml").text = checkstyleContent
    }

    private void copyJavaBuildDependencies() {
        String gradlePath = "./${project.projectName}/gradle"
        new File(gradlePath).mkdirs()
        new File("$gradlePath/java.gradle").text = getClass().getClassLoader().getResource("gradle-config/java.gradle").text
        new File("$gradlePath/jdepend.gradle").text = getClass().getClassLoader().getResource("gradle-config/jdepend.gradle").text
        new File("$gradlePath/docs.gradle").text = getClass().getClassLoader().getResource("gradle-config/docs.gradle").text
    }

    private void createHelloJavaFile(String packageAsPath) {
        String fullMainPath = "./${project.projectName}/src/main/java/${packageAsPath}"
        new File(fullMainPath).mkdirs()
        File helloWorld = new File("${fullMainPath}/HelloJava.java")
        String packageLine = "package ${project.rootPackageName};\n"
        String fileContent = packageLine + getClass().getClassLoader().getResource("HelloJava.txt").text
        helloWorld.text = fileContent
    }

    private void createHelloJavaTestFile(String packageAsPath) {
        String fullTestPath = "./${project.projectName}/src/test/java/${packageAsPath}"
        new File(fullTestPath).mkdirs()
        File helloWorldTest = new File("${fullTestPath}/HelloJavaTest.java")
        String packageLine = "package ${project.rootPackageName};\n"
        String fileContent = packageLine + getClass().getClassLoader().getResource("HelloJavaTest.txt").text
        helloWorldTest.text = fileContent
    }

}
