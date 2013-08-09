package com.ncc.common

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.Exec

/**
 * GruntTask adopted from http://naleid.com/blog/2013/01/24/calling-gruntjs-tasks-from-gradle/
 */
class GruntTask extends Exec {
    static final String GRUNT_EXECUTABLE = Os.isFamily(Os.FAMILY_WINDOWS) ? "grunt.cmd" : "grunt"

    GruntTask() {
        executable = GRUNT_EXECUTABLE
    }

    void gruntArgs(Object... gruntArgs) {
        this.args = gruntArgs as List
    }
}