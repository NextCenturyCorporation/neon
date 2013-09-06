package com.ncc.setup.command

import com.ncc.setup.create.ProjectCreator
import com.ncc.setup.model.Project

/**
 * @author tbrooks
 */

class CommandLine {

    Project projectModel

    CommandLine(){
        projectModel = new Project()
    }

    void setupProject() {
        printHeader()
        promptProjectName()
        promptPackageName()
        promptContainsJava()
        promptContainsGroovy()
    }

    private printHeader(){
        println "Next Century's project creator"
        println "------------------------------"
    }

    private void promptProjectName() {
        println "(Project names are typically all lower case with dashes separating words)"
        def value = prompt("Please enter the name of the project")
        projectModel.projectName = value
    }

    private void promptPackageName() {
        println "(Example: com.ncc.project)"
        def value = prompt("Enter package name")
        projectModel.rootPackageName = value
    }

    private void promptContainsJava() {
        def value = prompt("Does this code contain java (y/n)")
        boolean hasJava = false;
        if(value.toString().equalsIgnoreCase("y")){
            hasJava = true
        }
        projectModel.javaCode = hasJava

    }

    private void promptContainsGroovy() {
        def value = prompt("Does this code contain groovy (y/n)")
        boolean hasGroovy = false;
        if(value.toString().equalsIgnoreCase("y")){
            hasGroovy = true
        }
        projectModel.groovyCode = hasGroovy

    }

    private def prompt(String prompt){
        def console = System.console()
        console?.readLine "${prompt}: "
    }

    static void main(String [] args){
        CommandLine command = new CommandLine()
        command.setupProject()

        ProjectCreator creator = new ProjectCreator(command.projectModel)
        creator.createProject()
    }

}
